package sample;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import ru.common.Task;
import ru.common.TaskStatus;

public class Controller {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextArea ta;

    @FXML
    void initialize() {
        ta.setText("Server started\n");
        readAll();
        ta.setText(ta.getText()+Main.atomicTasks.get()+"\n");


        new Thread(()->{
            try (ServerSocket serverSocket = new ServerSocket(8000)){
                ExecutorService pool = Executors.newCachedThreadPool();
                while(true) {
                    Socket clientSocket = serverSocket.accept();
                    Main.clientsList.add(clientSocket);
                    pool.execute(()->handleConnection(clientSocket));
                    if(Main.clientsList.size()==0){
                        pool.shutdown();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();


    }

    @SuppressWarnings("unchecked")
    private void readAll() {
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(Main.file))) {
            ArrayList<Task> tasks = (ArrayList<Task>) ois.readObject();
            ta.setText(ta.getText()+"Session has been loaded\n");
            Main.atomicTasks = new AtomicReference<>(tasks);
        }
        catch(Exception ex){
            ta.setText(ta.getText()+ex.getMessage()+"\n");
        }
    }

    private void handleConnection(Socket clientSocket){
        while(!clientSocket.isClosed()){
            try {
                DataInputStream dis =new DataInputStream(clientSocket.getInputStream());
                String clientMessage = dis.readUTF();
                ta.setText(ta.getText() + "Message from " + Thread.currentThread().getName() + "\n");
                switch (clientMessage){
                    case "getTasks": {
                        ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                        out.writeObject(Main.atomicTasks.get());
                        ta.setText(ta.getText() + LocalTime.now() + ": список задач отправлен клиенту" + "\n");
                    }
                    break;
                    //запрос на добавление
                    case "addTask":
                    {
                        ObjectInputStream ois = new ObjectInputStream(dis);
                        Task createdTask=(Task)ois.readObject();
                        Main.atomicTasks.get().add(createdTask);
                        ta.setText(ta.getText() + LocalTime.now() + ": добавлена новая задача" + "\n");
                        sendAddedTaskToAllClients(clientSocket, createdTask, "add", -1);
                    }
                    break;
                    //запрос на обновление
                    case "updTask":
                    {
                        int index = dis.readInt();
                        ObjectInputStream ois = new ObjectInputStream(dis);
                        Task updatedTask=(Task)ois.readObject();
                        Main.atomicTasks.get().set(index, updatedTask);
                        sendAddedTaskToAllClients(clientSocket, updatedTask, "update", index);
                    }
                    break;
                    case "archTask":
                    {
                        int index = dis.readInt();
                        Main.atomicTasks.get().get(index).setTaskStatus(TaskStatus.Archieved);
                        sendAddedTaskToAllClients(clientSocket, null, "arch", index);
                    }
                    break;
                }
            } catch (IOException | ClassNotFoundException e) {
                ta.setText(ta.getText() + LocalTime.now() + "Похоже пропало соединение с клиентом: " + clientSocket.getInetAddress() + "\n");
                //e.printStackTrace();
                Main.clientsList.remove(clientSocket);
                try {
                    clientSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    private void sendAddedTaskToAllClients(Socket client, Task updTask, String type, int index) throws IOException {
        //пробуем отправить сообщение клиента 1 всем остальным клиентам
        for (Socket socket:
                Main.clientsList) {
            if(socket == client) continue;
            try{
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                switch (type)
                {
                    case "add":
                    {
                        dos.writeUTF("addFromServer");
                        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                        out.writeObject(updTask);
                    }
                    break;
                    case "update":
                    {
                        dos.writeUTF("updFromServer");
                        dos.writeInt(index);
                        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                        out.writeObject(updTask);
                    }
                    break;
                    case "arch":
                    {
                        dos.writeUTF("archFromServer");
                        dos.writeInt(index);
                    }
                    break;
                }
                ta.setText(ta.getText() + LocalTime.now() + "обновления отправлены" + "\n");
            }catch (IOException e){
                System.out.println("Похоже пропало соединение с клиентом: " + socket.getInetAddress());
                ta.setText(ta.getText() + LocalTime.now() + "Похоже пропало соединение с клиентом: " + socket.getInetAddress() + "\n");
                e.printStackTrace();
                Main.clientsList.remove(socket);
            }

        }
    }
}
