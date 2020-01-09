//package com.chick.operation.controller;
//
//import com.chick.operation.util.StringUtils;
//import java.io.IOException;
//import java.util.Set;
//import java.util.concurrent.CopyOnWriteArraySet;
//import javax.websocket.OnClose;
//import javax.websocket.OnError;
//import javax.websocket.OnMessage;
//import javax.websocket.OnOpen;
//import javax.websocket.Session;
//import javax.websocket.server.PathParam;
//import javax.websocket.server.ServerEndpoint;
//import lombok.extern.slf4j.Slf4j;
//
//@ServerEndpoint("/websocket/{username}")
//@Slf4j
//public class ExportTaskWebSocketController {
//
//  private static final Set<ExportTaskWebSocketController> connections = new CopyOnWriteArraySet<>();
//
//  private  String username;
//  private Session session;
//
//
//  @OnOpen
//  public void start(Session session,@PathParam("username") String username) {
//    this.session = session;
//    connections.add(this);
//    this.username = username;
//    String message = String.format("* %s %s", username, "has joined.");
//    broadcast(message);
//  }
//
//  @OnClose
//  public void end() {
//    connections.remove(this);
//    String message = String.format("* %s %s", username, "has disconnected.");
//    broadcast(message);
//  }
//
//  /**
//   * 收到客户端消息后调用的方法
//   *
//   * 客户端发送过来的消息
//   */
//  @OnMessage
//  public void incoming(String message) {
//    String filteredMessage = String.format("%s: %s", username, message.toString());
//    broadcast(filteredMessage);
//  }
//
//  @OnError
//  public void onError(Throwable t) throws Throwable {
//    log.error("Chat Error: " + t.toString(), t);
//  }
//
//  public void sendMessage(String message) throws IOException {
//    this.session.getBasicRemote().sendText(message);
//  }
//
//
//  /**
//   * 群发自定义消息
//   */
//  public static void sendInfo(String message, String username) throws IOException {
//    for (ExportTaskWebSocketController item : connections) {
//      try {
//        //这里可以设定只推送给这个sid的，为null则全部推送
//        if (StringUtils.isEmpty(username)) {
//          item.sendMessage(message);
//        } else if (item.username.equals(username)) {
//          item.sendMessage(message);
//        }
//      } catch (IOException e) {
//        continue;
//      }
//    }
//  }
//
//  private static void broadcast(String msg) {
//    for (ExportTaskWebSocketController client : connections) {
//      try {
//        synchronized (client) {
//          client.session.getBasicRemote().sendText(msg);
//        }
//      } catch (IOException e) {
//        log.debug("Chat Error: Failed to send message to client", e);
//        connections.remove(client);
//        try {
//          client.session.close();
//        } catch (IOException e1) {
//        }
//        String message = String.format("* %s %s", client.username, "has been disconnected.");
//        broadcast(message);
//      }
//    }
//  }
//}
