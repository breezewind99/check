package com.cnettech.util;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UdpClient {
    public static void SendMsg(String Server_Ip, int Server_Port, String Send_Msg) {
        try{
            // 전송할 수 있는 UDP 소켓 생성
            DatagramSocket dsoc = new DatagramSocket();
            // 받을 곳의 주소 생성
            InetAddress ia = InetAddress.getByName(Server_Ip);
            // 전소할 데이터 생성
            DatagramPacket dp = new DatagramPacket(Send_Msg.getBytes(),Send_Msg.getBytes().length,ia, Server_Port);
            // 메세지 전송
            dsoc.send(dp);
            dsoc.close();
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}
