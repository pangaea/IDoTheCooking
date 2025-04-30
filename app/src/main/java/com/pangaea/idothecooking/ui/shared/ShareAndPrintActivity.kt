package com.pangaea.idothecooking.ui.shared

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.print.PrintAttributes
import android.print.PrintManager
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.pangaea.idothecooking.R

open class ShareAndPrintActivity : AppCompatActivity() {

    // State
    private var selectedPhoneNumber: String? = null
    private var messageTitle: String? = null
    private var messageContent: String? = null

    object RequestCode {
        const val REQUEST_CONTACT = 0
        const val REQUEST_READ_CONTACTS_PERMISSIONS = 1
        const val REQUEST_SEND_SMS_PERMISSIONS = 2
    }

    fun sendMessage(title: String, content: String) {
        messageTitle = title
        messageContent = content
        if (hasContactsPermission()) {
            openContacts()
        } else {
            requestContactsPermission()
        }
    }

    private fun hasContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun hasSMSPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun requestContactsPermission() {
        if (!hasContactsPermission()) {
            ActivityCompat.requestPermissions(this,
                                              arrayOf<String>(Manifest.permission.READ_CONTACTS),
                                              RequestCode.REQUEST_READ_CONTACTS_PERMISSIONS)
        }
    }

    private fun requestSMSPermission() {
        if (!hasSMSPermission()) {
            ActivityCompat.requestPermissions(this,
                                              arrayOf<String>(Manifest.permission.SEND_SMS),
                                              RequestCode.REQUEST_SEND_SMS_PERMISSIONS)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RequestCode.REQUEST_READ_CONTACTS_PERMISSIONS && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openContacts()
            }
        } else if (requestCode == RequestCode.REQUEST_SEND_SMS_PERMISSIONS && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectedPhoneNumber?.let { sendSMS(it) }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("Range", "Recycle")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return
        if (requestCode == RequestCode.REQUEST_CONTACT && data != null) {
            val contactData = data.data
            if (contactData != null) {
                val cursor = contentResolver.query(contactData, null, null, null, null)
                if (cursor != null) {
                    try {
                        if (cursor.moveToFirst()) {
                            val id =
                                cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                            val hasPhone =
                                cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                            if (hasPhone.equals("1", ignoreCase = true)) {
                                val phones = contentResolver.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                                    null, null)
                                phones!!.moveToFirst()
                                val phoneNumbers: MutableList<String> = mutableListOf()
                                while (!phones.isAfterLast) {
                                    phoneNumbers.add(phones.getString(phones.getColumnIndex("data1")))
                                    phones.moveToNext()
                                }
                                if (phoneNumbers.isNotEmpty()) {
                                    PicklistDlg(getString(R.string.select_phone_number),
                                                phoneNumbers.map(){o -> Pair(o, o)}) { selectedNum: Pair<String, String> ->
                                        selectedPhoneNumber = selectedNum.second
                                        if (hasSMSPermission()) {
                                            sendSMS(selectedNum.second)
                                        } else {
                                            selectedPhoneNumber = selectedNum.second
                                            requestSMSPermission()
                                        }
                                    }.show(this.supportFragmentManager, null)
                                }
                            }
                        }
                    } finally {
                        cursor.close()
                    }
                }
            }
        }
    }

    private fun sendSMS(phoneNumber: String) {
        try {
            val smsManager = SmsManager.getDefault()
            val parts = smsManager.divideMessage(messageContent)
            smsManager.sendMultipartTextMessage(phoneNumber, null, parts,
                                                null, null)
            val successMsg = getString(R.string.recipe_sent_success).replace("{{0}}", messageTitle!!)
            Toast.makeText(applicationContext, successMsg, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            val failedMsg = getString(R.string.recipe_sent_failed).replace("{{0}}", messageTitle!!)
            Toast.makeText(applicationContext, failedMsg, Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun openContacts() {
        val pickContact = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        startActivityForResult(pickContact, RequestCode.REQUEST_CONTACT);
    }

    /////////////////////////////////////////////////////
    /////////////////////////////////////////////////
    /////////////////////////////////////////////

    fun createWebPrintJob(name: String, webView: WebView) {
        val printManager = getSystemService(PRINT_SERVICE) as PrintManager
        val printAdapter = webView.createPrintDocumentAdapter(name)
        printManager.print(name, printAdapter, PrintAttributes.Builder().build())
    }
}