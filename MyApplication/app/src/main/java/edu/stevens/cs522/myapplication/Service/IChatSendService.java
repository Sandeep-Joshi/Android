package edu.stevens.cs522.myapplication.Service;

import java.net.DatagramPacket;

/**
 * Created by Sandeep on 3/14/2015.
 */
public interface IChatSendService {
    public void send (DatagramPacket p); //Binders service method
}
