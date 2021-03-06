package info.xiaomo.server.server;

import com.google.protobuf.InvalidProtocolBufferException;
import info.xiaomo.gameCore.base.common.EncryptUtil;
import info.xiaomo.gameCore.base.concurrent.IQueueDriverCommand;
import info.xiaomo.gameCore.base.concurrent.queue.ICommandQueue;
import info.xiaomo.gameCore.protocol.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消息基类
 *
 * @author 张力
 * @date 2014-12-1
 */
@Data
public abstract class AbstractMessage implements Message {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMessage.class);

    /**
     * 命令队列
     */
    private ICommandQueue<IQueueDriverCommand> commandQueue;

    /**
     * 消息长度
     */
    private int length;

    /**
     * 一个额外的参数
     */
    protected Session session;

    /**
     * 过滤器
     */
    protected MessageFilter filter;

    protected short sequence;

    /**
     * 消息长度
     */
    private int size;

    /**
     * 队列ID
     */
    protected int queueId;

    /**
     * 解码
     *
     * @param bytes bytes
     */
    public void decode(byte[] bytes) throws InvalidProtocolBufferException {

    }

    public abstract int getId();

    @Override
    public void run() {
        try {
            long time = System.currentTimeMillis();
            if (filter != null && !filter.before(this)) {
                return;
            }
            LOGGER.error("执行消息:" + this);
            doAction();
            LOGGER.error(this + "耗时：" + (System.currentTimeMillis() - time));
        } catch (Throwable e) {
            LOGGER.error("命令执行错误", e);
        }
    }

    @Override
    public ICommandQueue<IQueueDriverCommand> getCommandQueue() {
        return commandQueue;
    }

    @Override
    public void setCommandQueue(ICommandQueue<IQueueDriverCommand> commandQueue) {
        this.commandQueue = commandQueue;
    }

    @Override
    public int length() {
        return this.length;
    }

    @Override
    public void setLength(int length) {
        this.length = length;
    }

    @Override
    public Session getParam() {
        return session;
    }

    @Override
    public void setParam(Object param) {
        this.session = (Session) param;
    }

    @Override
    public void setQueueId(int queueId) {
        this.queueId = queueId;
    }

    @Override
    public int getQueueId() {
        return this.queueId;
    }


    public String toString() {
        return "[id->" + getId() + ",sequence->" + sequence + "]";
    }

    public ByteBuf encode() {
        ByteBuf buf = null;
        try {

            byte[] content = getContent();
            content = EncryptUtil.encrypt(content);

            // 计算长度
            int size = 8;
            size += content.length;
            setSize(size);

            buf = ByteBufAllocator.DEFAULT.buffer(size);

            buf.writeInt(size);
            buf.writeInt(getId());
            // 写入消息体   0001111111110111
            buf.writeBytes(content);
            return buf;
        } catch (Exception e) {
            LOGGER.error("构建消息字节数组出错,id:" + this.getId(), e);
            if (buf != null) {
                buf.release();
            }
            return null;
        }
    }

    @Override
    public void doAction() {
    }

    @Override
    public byte[] getContent() {
        return null;
    }

}