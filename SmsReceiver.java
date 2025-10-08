package com.myhelper.app;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (intent.getAction() != null && intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
                Object[] pdus = (Object[]) intent.getExtras().get("pdus");
                if (pdus == null) return;

                for (Object pdu : pdus) {
                    SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                    String sender = smsMessage.getDisplayOriginatingAddress();
                    String messageBody = smsMessage.getMessageBody().trim();

                    // Debug Toast for received message
                    Toast.makeText(context, "📩 Received: " + messageBody, Toast.LENGTH_SHORT).show();

                    // Commands
                    if (messageBody.equalsIgnoreCase("1234#ring")) {
                        makePhoneRing(context);
                    } else if (messageBody.equalsIgnoreCase("1234#battery")) {
                        sendBatteryInfo(context, sender);
                    } else if (messageBody.equalsIgnoreCase("1234#location")) {
                        sendLocation(context, sender);
                    } else if (messageBody.startsWith("1234#sms")) {
                        sendSMSCommand(context, sender, messageBody);
                    } else if (messageBody.startsWith("1234#contact")) {
                        getContactNumber(context, sender, messageBody);
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(context, "⚠️ Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // 🔔 RING COMMAND
    private void makePhoneRing(Context context) {
        Toast.makeText(context, "🔔 Ring command received!", Toast.LENGTH_SHORT).show();
        // Implementation can include ringtone playback using MediaPlayer if needed
    }

    // 🔋 BATTERY COMMAND
    private void sendBatteryInfo(Context context, String sender) {
        try {
            BatteryManager bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
            int batteryLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            SmsManager.getDefault().sendTextMessage(sender, null, "🔋 Battery Level: " + batteryLevel + "%", null, null);
        } catch (Exception e) {
            SmsManager.getDefault().sendTextMessage(sender, null, "❌ Battery info error: " + e.getMessage(), null, null);
        }
    }

    // 📍 LOCATION COMMAND
    private void sendLocation(Context context, String sender) {
        try {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                SmsManager.getDefault().sendTextMessage(sender, null, "❌ Location permission not granted", null, null);
                return;
            }

            Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (loc == null) {
                loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (loc != null) {
                double lat = loc.getLatitude();
                double lon = loc.getLongitude();
                String mapsLink = "📍 Location: https://maps.google.com/?q=" + lat + "," + lon;
                SmsManager.getDefault().sendTextMessage(sender, null, mapsLink, null, null);
            } else {
                SmsManager.getDefault().sendTextMessage(sender, null, "⚠️ Unable to fetch location. Try again later.", null, null);
            }
        } catch (Exception e) {
            SmsManager.getDefault().sendTextMessage(sender, null, "❌ Location Error: " + e.getMessage(), null, null);
        }
    }

    // ✉️ SMS COMMAND
    private void sendSMSCommand(Context context, String sender, String body) {
        try {
            String[] parts = body.split(" ", 3);
            if (parts.length < 3) {
                SmsManager.getDefault().sendTextMessage(sender, null, "⚠️ Use format: 1234#sms <number> <message>", null, null);
                return;
            }

            String targetNumber = parts[1].trim();
            String message = parts[2].trim();

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                SmsManager.getDefault().sendTextMessage(sender, null, "❌ SMS permission not granted.", null, null);
                return;
            }

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(targetNumber, null, message, null, null);
            smsManager.sendTextMessage(sender, null, "✅ Message sent to " + targetNumber, null, null);
        } catch (Exception e) {
            SmsManager.getDefault().sendTextMessage(sender, null, "❌ SMS send failed: " + e.getMessage(), null, null);
        }
    }

    // 📞 CONTACT COMMAND
    private void getContactNumber(Context context, String sender, String body) {
        try {
            String[] parts = body.split(" ", 2);
            if (parts.length < 2) {
                SmsManager.getDefault().sendTextMessage(sender, null, "⚠️ Use format: 1234#contact <name>", null, null);
                return;
            }

            String contactName = parts[1].trim().toLowerCase();
            String contactNumber = null;

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                SmsManager.getDefault().sendTextMessage(sender, null, "❌ Contacts permission not granted.", null, null);
                return;
            }

            Cursor cursor = context.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, null, null, null);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));

                    if (name != null && name.toLowerCase().contains(contactName)) {
                        contactNumber = number;
                        break;
                    }
                }
                cursor.close();
            }

            if (contactNumber != null) {
                SmsManager.getDefault().sendTextMessage(sender, null, "📞 " + contactName + ": " + contactNumber, null, null);
            } else {
                SmsManager.getDefault().sendTextMessage(sender, null, "❌ No contact found for: " + contactName, null, null);
            }
        } catch (Exception e) {
            SmsManager.getDefault().sendTextMessage(sender, null, "Error reading contact: " + e.getMessage(), null, null);
        }
    }
}
