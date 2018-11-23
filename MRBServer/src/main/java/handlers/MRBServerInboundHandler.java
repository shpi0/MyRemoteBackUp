package handlers;

import files.FileMessageProcessor;
import files.FilePart;
import files.FileType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import message.FileMessage;
import message.FilePartMessage;
import message.MRBMessage;
import message.MessageType;
import models.User;
import services.UserService;
import util.PassUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class MRBServerInboundHandler extends ChannelInboundHandlerAdapter {

    private static final String ROOT_FOLDER = "data";
    private final static int MAX_FILE_PART_SIZE = 1024 * 1024 * 32;

    private FileMessageProcessor fileMessageProcessor = new FileMessageProcessor();
    private FilePart fp;

    private UserService userService = new UserService();
    private boolean userLoggedIn;
    private String userName;
    private LinkedList<String> folders = new LinkedList<>();

    private BlockingDeque<FilePart> filePartsToSend = new LinkedBlockingDeque<>();

    private String buildCurrentPath() {
        StringBuilder sb = new StringBuilder();
        sb.append(ROOT_FOLDER).append("/").append(userName).append("/");
        for (String s : folders) {
            sb.append(s).append("/");
        }
        return sb.toString();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client connected...");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg == null)
                return;
            if (msg instanceof MRBMessage) {
                switch (((MRBMessage) msg).getMessageType()) {
                    case FILE_PART_RECEIVED_SUCCESS:
                        pickAndWriteFilePart(ctx);
                        break;
                    case CREATE_FOLDER:
                        if (userLoggedIn) {
                            String folderName = (String) (((MRBMessage) msg).getData()).get(0);
                            try {
                                Files.createDirectory(Paths.get(buildCurrentPath() + folderName));
                                ctx.writeAndFlush(new MRBMessage(MessageType.CREATE_FOLDER_SUCCESS));
                            } catch (IOException e) {
                                ctx.writeAndFlush(new MRBMessage(MessageType.CREATE_FOLDER_FAIL));
                                e.printStackTrace();
                            }
                        }
                        break;
                    case FILE_RENAME:
                        if (userLoggedIn) {
                            String oldFileName = (String) (((MRBMessage) msg).getData()).get(0);
                            String newFileName = (String) (((MRBMessage) msg).getData()).get(1);
                            try {
                                Files.move(Paths.get(buildCurrentPath() + oldFileName), Paths.get(buildCurrentPath() + newFileName));
                                ctx.writeAndFlush(new MRBMessage(MessageType.FILE_RENAME_SUCCESS));
                            } catch (IOException e) {
                                ctx.writeAndFlush(new MRBMessage(MessageType.FILE_RENAME_FAIL));
                                e.printStackTrace();
                            }
                        }
                        break;
                    case FILE_REQUEST:
                        if (userLoggedIn) {
                            String selectedFile = ((ArrayList<String>) (((MRBMessage) msg).getData())).get(0);
                            String serverPath = ((ArrayList<String>) (((MRBMessage) msg).getData())).get(1);
                            try {
                                Path fileToSend = Paths.get(buildCurrentPath() + selectedFile);
                                if (Files.size(fileToSend) > MAX_FILE_PART_SIZE) {
                                    int partsCount = (int) (Files.size(fileToSend) / MAX_FILE_PART_SIZE);
                                    int lastPartSize = (int) (Files.size(fileToSend) % MAX_FILE_PART_SIZE);
                                    if (lastPartSize > 0) {
                                        partsCount++;
                                    }
                                    int partSize = MAX_FILE_PART_SIZE;
                                    for (int i = 0; i < partsCount; i++) {
                                        if (lastPartSize > 0 && i == partsCount - 1) {
                                            partSize = lastPartSize;
                                        }
                                        filePartsToSend.offer(new FilePart(i * MAX_FILE_PART_SIZE, null, selectedFile, serverPath, buildCurrentPath(), partSize, partsCount, i + 1));
                                    }
                                } else {
                                    filePartsToSend.offer(new FilePart(Paths.get(buildCurrentPath() + selectedFile), serverPath));
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            pickAndWriteFilePart(ctx);
                        }
                        break;
                    case REGISTER_REQUEST:
                        String receivedName = (String) (((MRBMessage) msg).getData()).get(0);
                        String receivedPass = (String) (((MRBMessage) msg).getData()).get(1);
                        String receivedPass2 = (String) (((MRBMessage) msg).getData()).get(2);
                        MessageType messageType = MessageType.REGISTER_FAIL;
                        if (receivedName != null && receivedPass != null && receivedPass2 != null) {
                            if (userService.findByName(receivedName) == null) {
                                if (receivedPass.equals(receivedPass2)) {
                                    userService.register(new User(receivedName, PassUtil.getInstance().getPassHash(receivedPass)));
                                    messageType = MessageType.REGISTER_DONE;
                                }
                            }
                        }
                        ctx.writeAndFlush(new MRBMessage(messageType));
                        break;
                    case FILE_DELETE:
                        if (userLoggedIn) {
                            Files.deleteIfExists(Paths.get(buildCurrentPath() + ((ArrayList<String>) (((MRBMessage) msg).getData())).get(0)));
                            ctx.writeAndFlush(new MRBMessage(MessageType.FILE_DELETE_OK));
                        }
                        break;
                    case LOGIN_ATTEMPT:
                        if (userService.authUser(((ArrayList<String>) (((MRBMessage) msg).getData())).get(0), PassUtil.getInstance().getPassHash(((ArrayList<String>) (((MRBMessage) msg).getData())).get(1)))) {
                            userLoggedIn = true;
                            userName = ((ArrayList<String>) (((MRBMessage) msg).getData())).get(0);
                            ctx.writeAndFlush(new MRBMessage(MessageType.LOGIN_SUCCESS));
                        } else {
                            ctx.writeAndFlush(new MRBMessage(MessageType.LOGIN_FAILED));
                        }
                        break;
                    case FOLDER_CHANGE:
                        if (userLoggedIn) {
                            String newFolderName = (String) (((MRBMessage) msg).getData()).get(0);
                            if (newFolderName != null && !"".equals(newFolderName)) {
                                if ("[..]".equals(newFolderName)) {
                                    folders.removeLast();
                                } else {
                                    folders.add(newFolderName.substring(1, newFolderName.length() - 1));
                                }
                            }
                        }
                    case FILE_LIST_REQUEST:
                        if (userLoggedIn) {
                            if (!Files.exists(Paths.get(buildCurrentPath()))) {
                                Files.createDirectory(Paths.get(buildCurrentPath()));
                            }
                            Map<String, FileType> filesMap = new HashMap<>();
                            ArrayList<String> filesList = new ArrayList<>();
                            if (!buildCurrentPath().equals(ROOT_FOLDER + "/" + userName + "/")) {
                                filesMap.put("[..]", FileType.FOLDER);
                                filesList.add("[..]");
                            }
                            Files.list(Paths.get(buildCurrentPath()))
                                    .filter(path -> Files.isDirectory(path))
                                    .map(p -> p.getFileName().toString())
                                    .forEach(o -> {
                                        o = "[" + o + "]";
                                        filesMap.put(o, FileType.FOLDER);
                                        filesList.add(o);
                                    });
                            Files.list(Paths.get(buildCurrentPath()))
                                    .filter(path -> !Files.isDirectory(path))
                                    .map(p -> p.getFileName().toString())
                                    .forEach(o -> {
                                        filesMap.put(o, FileType.FILE);
                                        filesList.add(o);
                                    });
                            ctx.writeAndFlush(new MRBMessage(MessageType.FILE_LIST, filesList, filesMap, buildCurrentPath()));
                        }
                        break;
                }
            } else {
                if (msg instanceof FileMessage) {
                    if (userLoggedIn) {
                        try {
                            fileMessageProcessor.writeIncomingFileMessageToDisk((FileMessage) msg, buildCurrentPath(), MAX_FILE_PART_SIZE);
                            ctx.writeAndFlush(new MRBMessage(MessageType.FILE_RECEIVED_SUCCESS));
                        } catch (IOException e) {
                            ctx.writeAndFlush(new MRBMessage(MessageType.FILE_RECEIVED_FAIL));
                            e.printStackTrace();
                        }
                    }
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void pickAndWriteFilePart(ChannelHandlerContext ctx) {
        fp = filePartsToSend.poll();
        if (fp != null) {
            ctx.writeAndFlush(new FilePartMessage(fp));
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
