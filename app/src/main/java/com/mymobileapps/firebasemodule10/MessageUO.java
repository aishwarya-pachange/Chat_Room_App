/*
 *************** ASSIGNMENT# - In Class Assignment 09 ***************
 *************** FILE NAME - MessageUO.java ***************
 *************** FULL NAME - Aishwarya Nandkumar Pachange & Janani Krishnan (Group 18) ***************
 */
package com.mymobileapps.firebasemodule10;

import android.media.Image;


public class MessageUO {


    String chatId, messageText, timeCreated, fName,lName, imageDetail;

    public MessageUO(String chatId, String messageText, String timeCreated, String fName, String lName, String imageDetail) {
        this.chatId = chatId;
        this.messageText = messageText;
        this.timeCreated = timeCreated;
        this.fName = fName;
        this.lName = lName;
        this.imageDetail = imageDetail;
    }

    public MessageUO() {
    }

    @Override
    public String toString() {
        return "MessageUO{" +
                "chatId='" + chatId + '\'' +
                ", messageText='" + messageText + '\'' +
                ", timeCreated='" + timeCreated + '\'' +
                ", fName='" + fName + '\'' +
                ", lName='" + lName + '\'' +
                ", imageDetail='" + imageDetail + '\'' +
                '}';
    }
}
