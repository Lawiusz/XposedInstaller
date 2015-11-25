package de.robv.android.xposed.installer.util;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


import static de.robv.android.xposed.installer.R.*;


public class LoadLog extends AsyncTask<File, Integer, String> {

    public Context mcontext;
    private static final int MAX_LOG_SIZE = 100 * 1024; // 100 KB
    private View rootView;
    public LoadLog(Context context, View rootView ){
        this.mcontext = context;
        this.rootView = rootView;

    }

    private long skipLargeFile(BufferedReader is, long length) throws IOException {
        if (length < MAX_LOG_SIZE)
            return 0;

        long skipped = length - MAX_LOG_SIZE;
        long yetToSkip = skipped;
        do {
            yetToSkip -= is.skip(yetToSkip);
        } while (yetToSkip > 0);

        int c;
        do {
            c = is.read();
            if (c == -1)
                break;
            skipped++;
        } while (c != '\n');

        return skipped;

    }

                    @Override
                    protected String doInBackground(File... log) {
                        Thread.currentThread().setPriority(Thread.NORM_PRIORITY+2);

                        StringBuffer llog = new StringBuffer(15 * 10 * 1024);
                        try {
                            File logfile = log[0];
                            BufferedReader br;
                            br = new BufferedReader(new FileReader(logfile));
                            long skipped = skipLargeFile(br, logfile.length());
                            if (skipped > 0) {
                                String SmaxLogSize = Integer.toString(MAX_LOG_SIZE / 1024);
                                String Sskipped = Long.toString(skipped / 1024);
                                llog.append("-----------------\n");
                                llog.append("Log too large. Allowed "+SmaxLogSize+"KB, skipped " +Sskipped+" KB");
                                llog.append("\n-----------------\n\n");
                            }


                            char[] temp = new char[1024];
                            int read;
                            while ((read = br.read(temp)) > 0) {
                                llog.append(temp, 0, read);
                            }
                            br.close();
                        } catch (IOException e) {
                            llog.append("Cannot read log");
                            llog.append(e.getMessage());

                        }

                        return llog.toString();
                    }

                    protected void onPostExecute(String llog) {
                        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                        TextView mTxtLog;
                        mTxtLog = (TextView) rootView.findViewById(id.txtLog);
                        mTxtLog.setText(llog);
                        }
                    }
