package fluvial.model.performer;

import fluvial.comm.ErrorCode;
import fluvial.comm.TcpSender;

import java.util.Date;

/**
 * Created by superttmm on 06/07/2017.
 */
public class PerformerModule {

    public PerformerModule(){}

    public PerformerModule(String name){
        this.name = name;
    }

    private String name;

    private String ip;

    private String port;

    private String protocol;

    private Date updateTime;

    private boolean isOnline;

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public String getIp() {return ip;}

    public void setIp(String ip) {this.ip = ip;}

    public String getPort() {return port;}

    public void setPort(String port) {this.port = port;}

    public String getProtocol() {return protocol;}

    public void setProtocol(String protocol) {this.protocol = protocol;}

    public Date getUpdateTime() {return updateTime;}

    public void setUpdateTime(Date date) {this.updateTime = date;}

    public boolean getIsOnline(){
        if(this.updateTime == null){
            return false;
        }
        return System.currentTimeMillis() - updateTime.getTime() < 30 * 1000;
    }

    public ErrorCode actCommand(String command){
        if(command == null){
            return ErrorCode.CommandNotValidException;
        }
        if(protocol == null){
            return ErrorCode.ProtocolNotSpecifiedException;
        }
        if(protocol.equals("Tcp")){
            TcpSender sender = new TcpSender(ip, port);
            return sender.send(command);
        }
        return ErrorCode.ProtocolNotSupportedException;
    }

    public void updateModule(PerformerModule module){
        setName(module.getName());
        setIp(module.getIp());
        setPort(module.getPort());
        setProtocol(module.getProtocol());
    }

    @Override
    public String toString(){
        return "name:" + name + "\n" +
                "ip:" + ip + "\n" +
                "port:" + port + "\n" +
                "protocol:" + protocol;
    }
}
