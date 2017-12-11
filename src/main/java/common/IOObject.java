package common;

import exception.IOQueryException;
import query.Query;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class IOObject {
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    public IOObject(Socket socket) {
        this.socket = socket;
        try {
            outputStream = new ObjectOutputStream(socket.getOutputStream());
//            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Query read() throws IOQueryException {
        try {
            inputStream = new ObjectInputStream(socket.getInputStream());
            return (Query) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new IOQueryException("Read error", e);
        }
    }

    public void write(Query query) throws IOQueryException {
        try {
            outputStream.writeObject(query);
            outputStream.flush();
        } catch (IOException e) {
            throw new IOQueryException("Write error", e);
        }
    }

}
