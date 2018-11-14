package handlers;

import files.FileMessageProcessor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import message.FileMessage;
import message.MRBMessage;
import message.MessageType;
import models.User;
import services.UserService;
import util.PassUtil;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MRBServerInboundHandler extends ChannelInboundHandlerAdapter {

    private UserService userService = new UserService();
    private boolean userLoggedIn;
    private String userName;


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client connected...");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg == null)
                return;
            System.out.println(msg.getClass());
            if (msg instanceof MRBMessage) {
                switch (((MRBMessage) msg).getMessageType()) {
                    case FILE_REQUEST:
                        if (userLoggedIn) {
                            ctx.writeAndFlush(FileMessageProcessor.getInstance().generateFileMessage(Paths.get("data/" + userName + "/" + ((ArrayList<String>) (((MRBMessage) msg).getData())).get(0))));
                        }
                        break;
                    case REGISTER_REQUEST:
                        System.out.println("reg try");
                        String receivedName = (String)(((MRBMessage) msg).getData()).get(0);
                        String receivedPass = (String)(((MRBMessage) msg).getData()).get(1);
                        String receivedPass2 = (String)(((MRBMessage) msg).getData()).get(2);
                        System.out.println(receivedName + " " + receivedPass + " " + receivedPass2);
                        MessageType messageType = MessageType.REGISTER_FAIL;
                        if (receivedName != null && receivedPass != null && receivedPass2 != null) {
                            System.out.println("1");
                            if (userService.findByName(receivedName) == null) {
                                System.out.println("2");
                                if (receivedPass.equals(receivedPass2)) {
                                    System.out.println("3");
                                    userService.register(new User(receivedName, PassUtil.getInstance().getPassHash(receivedPass)));
                                    messageType = MessageType.REGISTER_DONE;
                                }
                            }
                        }
                        ctx.writeAndFlush(new MRBMessage(messageType));
                        break;
                    case FILE_DELETE:
                        if (userLoggedIn) {
                            Files.deleteIfExists(Paths.get("data/" + userName + "/" + ((ArrayList<String>) (((MRBMessage) msg).getData())).get(0)));
                            ctx.writeAndFlush(new MRBMessage(MessageType.FILE_DELETE_OK));
                        }
                        break;
                    case LOGIN_ATTEMPT:
                        System.out.println("auth try");
                        if (userService.authUser(((ArrayList<String>) (((MRBMessage) msg).getData())).get(0), PassUtil.getInstance().getPassHash(((ArrayList<String>) (((MRBMessage) msg).getData())).get(1)))) {
                            System.out.println("Auth user ok");
                            userLoggedIn = true;
                            userName = ((ArrayList<String>) (((MRBMessage) msg).getData())).get(0);
                            ctx.writeAndFlush(new MRBMessage(MessageType.LOGIN_SUCCESS));
                        } else {
                            System.out.println("auth fail");
                            ctx.writeAndFlush(new MRBMessage(MessageType.LOGIN_FAILED));
                        }
                        break;
                    case FILE_LIST_REQUEST:
                        if (userLoggedIn) {
                            if (!Files.exists(Paths.get("data/" + userName))) {
                                Files.createDirectory(Paths.get("data/" + userName));
                            }
                            List<String> fileList = new ArrayList<>();
                            Files.list(Paths.get("data/" + userName)).map(p -> p.getFileName().toString()).forEach(o -> fileList.add(o));
                            ctx.writeAndFlush(new MRBMessage(MessageType.FILE_LIST, fileList));
                        }
                        break;
                }
            } else {
                if (msg instanceof FileMessage) {
                    if (userLoggedIn) {
                        FileMessageProcessor.getInstance().fileMessageProcess(userName, (FileMessage) msg);
                        ctx.writeAndFlush(new MRBMessage(MessageType.FILE_RECEIVED_SUCCESS));
                    }
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}
