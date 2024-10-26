
//package com.wxius.framework.zoo.core.dto;
//
///**
// * @author Jason Song(song_s@ctrip.com)
// */
//public class ZooConfigNotification {
//  private String namespaceName;
//  private long notificationId;
//  private volatile ZooNotificationMessages messages;
//
//  //for json converter
//  public ZooConfigNotification() {
//  }
//
//  public ZooConfigNotification(String namespaceName, long notificationId) {
//    this.namespaceName = namespaceName;
//    this.notificationId = notificationId;
//  }
//
//  public String getNamespaceName() {
//    return namespaceName;
//  }
//
//  public long getNotificationId() {
//    return notificationId;
//  }
//
//  public void setNamespaceName(String namespaceName) {
//    this.namespaceName = namespaceName;
//  }
//
//  public ZooNotificationMessages getMessages() {
//    return messages;
//  }
//
//  public void setMessages(ZooNotificationMessages messages) {
//    this.messages = messages;
//  }
//
//  public void addMessage(String key, long notificationId) {
//    if (this.messages == null) {
//      synchronized (this) {
//        if (this.messages == null) {
//          this.messages = new ZooNotificationMessages();
//        }
//      }
//    }
//    this.messages.put(key, notificationId);
//  }
//
//  @Override
//  public String toString() {
//    return "ZooConfigNotification{" +
//        "namespaceName='" + namespaceName + '\'' +
//        ", notificationId=" + notificationId +
//        '}';
//  }
//}
