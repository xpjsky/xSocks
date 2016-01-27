package net.iampaddy.socks.socket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * Created by pxie on 1/27/16.
 */
public class RemoteReadHandler implements CompletionHandler<Integer, ByteBuffer> {

    private Logger logger = LoggerFactory.getLogger(RemoteReadHandler.class);

    private DestKey destKey;
    private ChannelHandlerContext context;
    private AsynchronousSocketChannel remoteChannel;

    public RemoteReadHandler(DestKey destKey, ChannelHandlerContext ctx, AsynchronousSocketChannel remoteChannel) {
        this.destKey = destKey;
        this.context = ctx;
        this.remoteChannel = remoteChannel;
    }

    @Override
    public void completed(Integer result, ByteBuffer buffer) {
        if (result == -1) {
            SocketManager.getInstance().disconnect(destKey, remoteChannel);
            context.close();
            logger.info("{} - remote channel closed", destKey);
            return;
        }
        ByteBuf buf = context.alloc().buffer(result);
        buffer.flip();
        buf.writeBytes(buffer);
        context.writeAndFlush(buf);
        buffer.clear();

        logger.debug("{} - receive data {}", destKey, result);

        remoteChannel.read(buffer, buffer, this);
    }

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
        logger.error("{} - read error:", destKey);
        logger.error("", exc);
    }
}
