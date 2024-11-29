package org.example.tool;

import java.io.*;

public class ExecStream extends Thread {
    private InputStream is;
    private String type;
    private OutputStream os;

    public ExecStream(InputStream is, String type) {
        this.is = is;
        this.type = type;
    }

    public ExecStream(InputStream is, String type, OutputStream os) {
        this.is = is;
        this.type = type;
        this.os = os;
    }

    @Override
    public void run() {
        InputStreamReader isr = null;
        BufferedReader br = null;
        PrintWriter pw = null;

        try {
            if (os != null){
                pw = new PrintWriter(os);
            }

            isr = new InputStreamReader(is);
            br = new BufferedReader(isr);
            String line = null;

            while ((line = br.readLine()) != null) {
                if (pw != null) {
                    pw.println(line);
                }
            }

            if (pw != null) {
                pw.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                pw.close();
                br.close();
                isr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}