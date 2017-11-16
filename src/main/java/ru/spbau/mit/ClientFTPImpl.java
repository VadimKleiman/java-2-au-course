package ru.spbau.mit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class ClientFTPImpl implements ClientFTP {
    private String address;
    private int port;
    private DataInputStream in;
    private DataOutputStream out;
    private Socket socket;

    public ClientFTPImpl(String address, int port)
    {
        this.address = address;
        this.port = port;
    }

    @Override
    public boolean connect() {
        try {
            InetAddress ipAddress = InetAddress.getByName(address);
            socket = new Socket(ipAddress, port);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            return true;
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    @Override
    public void close() throws Exception{
        try {
            out.writeInt(0);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public ArrayList<Tuple<String, Boolean>> executeList(String path) {
        try {
            out.writeInt(1);
            out.writeUTF(path);
            int count = in.readInt();
            ArrayList<Tuple<String, Boolean>> out = new ArrayList<>();
            for (int i = 0; i < count; ++i)
            {
                out.add(new Tuple<>(in.readUTF(), in.readBoolean()));
            }
            return out;
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public ArrayList<Byte> executeGet(String path) {
        try {
            out.writeInt(2);
            out.writeUTF(path);
            long size = in.readLong();
            ArrayList<Byte> out = new ArrayList<>();
            for (long i = 0; i < size; ++i)
            {
                out.add(in.readByte());
            }
            return out;
        } catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
        return null;
    }
}
