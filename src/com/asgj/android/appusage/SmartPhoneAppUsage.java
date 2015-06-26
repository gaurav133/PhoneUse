package com.asgj.android.appusage;


import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

import com.asgj.android.appusage.service.JsonSender;

@ReportsCrashes(formKey = "", // will not be used
formUri = "http://mandrillapp.com/api/1.0/messages/send.json"
)

public class SmartPhoneAppUsage extends Application {
private ReportsCrashes mReportsCrashes;

@Override
public void onCreate() {
super.onCreate();

ACRA.init(this);

mReportsCrashes = this.getClass().getAnnotation(ReportsCrashes.class);
JsonSender jsonSender = new JsonSender(mReportsCrashes.formUri(), null);
ACRA.getErrorReporter().setReportSender(jsonSender);

}
}
