package com.asgj.android.appusage;

import java.lang.Thread.UncaughtExceptionHandler;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

import com.asgj.android.appusage.service.JsonSender;

@ReportsCrashes(formKey = "", // will not be used
formUri = "http://mandrillapp.com/api/1.0/messages/send.json"
)

public class SmartPhoneAppUsage extends Application {
    private ReportsCrashes mReportsCrashes;
    
    private Thread.UncaughtExceptionHandler androidDefaultUEH;
    private UncaughtExceptionHandler handler = new UncaughtExceptionHandler() {
        
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            // TODO Auto-generated method stub
            
            androidDefaultUEH.uncaughtException(thread, ex);
            System.exit(2);
        }
    };

@Override
public void onCreate() {
super.onCreate();

        androidDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(handler);
        ACRA.init(this);

mReportsCrashes = this.getClass().getAnnotation(ReportsCrashes.class);
JsonSender jsonSender = new JsonSender(mReportsCrashes.formUri(), null);
ACRA.getErrorReporter().setReportSender(jsonSender);

}
}
