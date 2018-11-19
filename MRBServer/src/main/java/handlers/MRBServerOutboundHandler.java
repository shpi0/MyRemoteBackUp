package handlers;

import files.FileMessageProcessor;
import files.FilePart;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import message.FileMessage;
import message.FilePartMessage;

public class MRBServerOutboundHandler extends ChannelOutboundHandlerAdapter {

    private final static int MAX_FILE_PART_SIZE = 1024 * 1024 * 32;

    private FileMessageProcessor fileMessageProcessor = new FileMessageProcessor();
    private byte[] data = new byte[MAX_FILE_PART_SIZE];
    private FilePart fp;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        System.out.println("Writing message " + msg.getClass());
        if (msg instanceof FilePartMessage) {
            fp = ((FilePartMessage) msg).getFp();
            if (fp.getFileData() == null) {
                fileMessageProcessor.addFileDataToFilePart(fp, MAX_FILE_PART_SIZE, data);
            }
            if (fp.getFileData() != null) {
                System.out.println("Sending message part " + fp.getCurrentPartNum() + " of " + fp.getTotalParts() + ", filename: " + fp.getFileName());
                msg = new FileMessage(fp.getFileName(), fp.getFilePath(), fp.getFileData(), fp.getTotalParts(), fp.getCurrentPartNum());
//                ctx.writeAndFlush(new FileMessage(fp.getFileName(), fp.getFilePath(), fp.getFileData(), fp.getTotalParts(), fp.getCurrentPartNum()));
//                ctx.writeAndFlush(FileMessageProcessor.getInstance().generateFileMessage(Paths.get(buildCurrentPath() + ((ArrayList<String>) (((MRBMessage) msg).getData())).get(0))));
            } else {
                System.out.println("WARNING! File with null data detected.");
            }
        }
        ctx.writeAndFlush(msg);
/*        super.write(ctx, msg, promise);
        super.flush(ctx);*/
        System.out.println("===============================");
    }

}
