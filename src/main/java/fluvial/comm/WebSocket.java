package fluvial.comm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * Created by superttmm on 31/05/2017.
 */
@Controller
public class WebSocket {
    private final SimpMessagingTemplate webSocket;

    @Autowired
    public WebSocket(SimpMessagingTemplate webSocket){
        this.webSocket = webSocket;
        this.webSocket.setSendTimeout(3000L);
    }

    public void send(String destination, Object payload){
        webSocket.convertAndSend(destination, payload);
    }
}
