// ICustomerScreenService.aidl
package com.token.v1.os.launcher;

// Declare any non-default types here with import statements

interface ICustomerScreenService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */

   //TextSize SMALL = 0, NORMAL = 1 ; BIG = 2
   //Generic Functions
   void setOnlyText(String mainStr, int textSize);
   void setHeaderAndText(String headerStr, int headerTextSize,String mainStr,  int mainTextSize);
   void setHeaderAndImage(String headerStr, int textSize, int imgResourceId);
   void setProgressBar(String headerStr, int textSize);

   //Specific functions
   void setOK();
   void setDenied();
   void setInsertCard();

   void setVideo(String videoPath);
   void setQR(String header, String total, String QRData);
}
