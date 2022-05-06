package com.example.phonebook

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.phonebook.databinding.ContentproviderFragmentBinding

const val REQUEST_CODE_READ_CONTACTS = 42
const val REQUEST_CODE_CALL_PHONE = 43


class ContentProviderFragment : Fragment() {
    private var _binding: ContentproviderFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ContentproviderFragmentBinding.inflate(inflater, container,
            false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermission(REQUEST_CODE_READ_CONTACTS,"")
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    companion object {
        @JvmStatic
        fun newInstance() =
            ContentProviderFragment()
    }

    private fun checkPermission(requestCode: Int, phone :String){
        context?.let {
            when(requestCode){
                REQUEST_CODE_READ_CONTACTS ->{
                    when {
                        ContextCompat.checkSelfPermission(it,
                            Manifest.permission.READ_CONTACTS) ==
                                PackageManager.PERMISSION_GRANTED -> {
                            getContacts()
                        }
                        else -> requestPermission(requestCode)
                    }
                }
                REQUEST_CODE_CALL_PHONE ->{
                    when {
                        ContextCompat.checkSelfPermission(it,
                            Manifest.permission.CALL_PHONE) ==
                                PackageManager.PERMISSION_GRANTED -> {
                            phoneCall(phone)
                        }
                        else -> requestPermission(requestCode)
                    }
                }
            }

        }
    }

    private fun requestPermission(requet_code: Int) {
        when(requet_code){
            REQUEST_CODE_READ_CONTACTS -> requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_CODE_READ_CONTACTS)
            REQUEST_CODE_CALL_PHONE -> requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), REQUEST_CODE_CALL_PHONE)
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_READ_CONTACTS -> {
                // Проверяем, дано ли пользователем разрешение по нашему запросу
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    getContacts()
                }
                return
            }
        }
    }

    private fun getContacts(){
        context?.let {
            // Получаем ContentResolver у контекста
            val contentResolver: ContentResolver = it.contentResolver
            // Отправляем запрос на получение контактов и получаем ответ в виде Cursor
            val cursorWithContacts: Cursor? = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME + " ASC"
            )

            cursorWithContacts?.let { cursor ->
                val contactId_index = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                val name_index = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                val has_phone_index = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                for (i in 0..cursor.count) {
                    // Переходим на позицию в Cursor
                    if (cursor.moveToPosition(i)) {
                        // Берём из Cursor столбец с именем
                        val name = cursor.getString(name_index)
                        val contact_id = cursor.getString(contactId_index)
                        val has_phone = cursor.getString(has_phone_index)
                        addView(it, name)
                        if(has_phone.equals("1")){
                            GetPhones(contentResolver,contact_id)
                        }
                    }
                }
            }
            cursorWithContacts?.close()
        }
    }

    private fun GetPhones(contentResolver : ContentResolver, contact_id : String){
        context?.let{
        val cursorWithContacts: Cursor? = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ contact_id,
            null,
            null
        )
        cursorWithContacts?.let { cursor ->
            val phone_index = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            for (j in 0..cursor.count){
                if (cursor.moveToPosition(j)) {
                    val phone = cursor.getString(phone_index)
                    addViewPhone(it, phone)
                }
            }
        }
        cursorWithContacts?.close()
        }
    }

    private fun addView(context: Context, textToShow: String) {
        binding.containerForContacts.addView(AppCompatTextView(context).apply {
            text = textToShow
            textSize = resources.getDimension(R.dimen.contact_size)
        })
    }

    private fun addViewPhone(context: Context, textToShow: String) {
        binding.containerForContacts.addView(AppCompatTextView(context).apply {
            text = textToShow
            textSize = resources.getDimension(R.dimen.phone_size)
            setOnClickListener {
                checkPermission(REQUEST_CODE_CALL_PHONE,textToShow)
            }
        })
    }


    private fun phoneCall(phone : String){
        //Toast.makeText(context,phone,Toast.LENGTH_LONG).show()
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
        startActivity(intent);
    }



}
