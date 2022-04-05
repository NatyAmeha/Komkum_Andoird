package com.komkum.komkum.ui.user

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.squareup.picasso.Picasso
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R
import com.komkum.komkum.data.model.UserWithSubscription
import com.komkum.komkum.databinding.FragmentProfileBinding
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.viewhelper.CircleTransformation
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

import android.provider.MediaStore
import androidx.activity.addCallback
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.MutableLiveData
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.extensions.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.FileOutputStream
import android.content.ContentResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream


@AndroidEntryPoint
class ProfileFragment : Fragment() {

    lateinit var binding : FragmentProfileBinding
    val userViewmodel : UserViewModel by viewModels()
    var user : UserWithSubscription? = null

    var uploadedImageUri : String? = null

    var canUploadProfilePhoto = false   // value will be true if user give read storage permission
    var isEditStarted = MutableLiveData(false)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        arguments?.let {
            user = it.getParcelable("USER")
        }
        binding = FragmentProfileBinding.inflate(inflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar , getString(R.string.edit_profile))
        var preference = PreferenceHelper.getInstance(requireContext())
        var profilePicture = preference.getString(AccountState.PROFILE_IMAGE, "")
        if (!profilePicture.isNullOrBlank()) Picasso.get().load(profilePicture)
            .placeholder(R.drawable.ic_person_black_24dp).transform(CircleTransformation())
            .into(binding.imageView9)

        this.requestPermissionFragment(listOf(Manifest.permission.READ_EXTERNAL_STORAGE) ,
            getString(R.string.storage_permission_msg)  , 111){
            canUploadProfilePhoto = true
        }

        binding.usernameEditText.setText(user?.username)
        binding.emailEditText.setText(user?.email)
        binding.phoneNumberEditText.setText(user?.phoneNumber)

        isEditStarted.observe(viewLifecycleOwner){
            binding.saveProfileBtn.isEnabled = it
        }

        binding.textInputLayout2.setEndIconOnClickListener {
            binding.usernameEditText.isEnabled = !binding.usernameEditText.isEnabled
//            binding.usernameEditText.isFocusable = binding.usernameEditText.isEnabled
            binding.textInputLayout2.endIconDrawable =
                if(binding.usernameEditText.isEnabled) resources.getDrawable(R.drawable.ic_clear_black_24dp)
                else resources.getDrawable(R.drawable.ic_mode_edit_black_24dp)
        }



        binding.textInputLayout3.setEndIconOnClickListener {
            binding.emailEditText.isEnabled = !binding.emailEditText.isEnabled
//            binding.emailEditText.isFocusable = binding.emailEditText.isEnabled
            binding.textInputLayout3.endIconDrawable =
                if(binding.emailEditText.isEnabled) resources.getDrawable(R.drawable.ic_clear_black_24dp)
                else resources.getDrawable(R.drawable.ic_mode_edit_black_24dp)
        }
        binding.textInputLayout5.setEndIconOnClickListener {
            binding.phoneNumberEditText.isEnabled = !binding.phoneNumberEditText.isEnabled
            binding.countryCodePicker.isVisible = binding.phoneNumberEditText.isEnabled
//            binding.phoneNumberEditText.isFocusable = binding.phoneNumberEditText.isEnabled
            binding.textInputLayout5.endIconDrawable =
                if(binding.phoneNumberEditText.isEnabled) resources.getDrawable(R.drawable.ic_clear_black_24dp)
                else resources.getDrawable(R.drawable.ic_mode_edit_black_24dp)
        }

        binding.usernameEditText.doOnTextChanged { text, start, before, count -> isEditStarted.value = true }
        binding.emailEditText.doOnTextChanged { text, start, before, count -> isEditStarted.value = true }
        binding.phoneNumberEditText.doOnTextChanged { text, start, before, count -> isEditStarted.value = true }

        binding.uploadImageview.setOnClickListener {
            if(canUploadProfilePhoto) pickImage()
            else (requireActivity() as MainActivity).showDialog(getString(R.string.permission_error) , getString(R.string.storage_permission_msg) , getString(R.string.give_permission)){
                requireActivity().handlePermissionStatus(listOf(Manifest.permission.READ_EXTERNAL_STORAGE) ,
                    getString(R.string.location_permission_for_profile) , 111,
                    onFailure = {
                        requireActivity().gotoPermissionSetting()
                    }){
                    canUploadProfilePhoto = true
                }
            }
        }

        binding.saveProfileBtn.setOnClickListener {
            binding.profileUpdateProgressbar.isVisible = true
            updateUserInfo(preference)
        }



        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            if(isEditStarted.value == true)
                (requireActivity() as MainActivity).showDialog("${getString(R.string.discard)}" , positiveButtonText = "Exit", showNegative = true){
                    findNavController().navigateUp()
                }
            else findNavController().navigateUp()
        }


        userViewmodel.getError().observe(viewLifecycleOwner){}
        userViewmodel.error.observe(viewLifecycleOwner){
            it.handleError(requireContext()){
                (requireActivity() as MainActivity).showErrorSnacbar("Error occurred.\n${it}", "Dismiss"){}
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home ->{
                if(isEditStarted.value == true)
                    (requireActivity() as MainActivity).showDialog("${getString(R.string.discard)}" , positiveButtonText = "Exit", showNegative = true){
                        findNavController().navigateUp()
                    }
                else findNavController().navigateUp()

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == Activity.RESULT_OK){
           data?.data?.let {
               binding.profileUpdateProgressbar.isVisible = true
               Picasso.get().load(it).fit().transform(CircleTransformation()).into(binding.imageView9)
               CoroutineScope(Dispatchers.IO).launch{
                   uploadedImageUri = createCopyAndReturnRealPath(requireContext() , it)
                   withContext(Dispatchers.Main){
                       binding.profileUpdateProgressbar.isVisible = false
                       isEditStarted.value = true
                   }
               }
           }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            111 -> {
                requireActivity().handlePermissionStatus(listOf(Manifest.permission.READ_EXTERNAL_STORAGE) ,
                    getString(R.string.permission_for_profile_upgrade) , 111,
                onFailure = {
                    requireActivity().gotoPermissionSetting()
                }){
                    canUploadProfilePhoto = true
                }
            }
        }
    }

    fun pickImage(){
        var intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent , 11)
    }



    fun updateUserInfo(preference : SharedPreferences){
        var email = binding.emailEditText.text.toString()
        var phoneNumber = if(!binding.phoneNumberEditText.text.isNullOrEmpty()){
            binding.countryCodePicker.registerCarrierNumberEditText(binding.phoneNumberEditText)
            binding.countryCodePicker.fullNumberWithPlus
        }
        else ""

        var username = binding.usernameEditText.text.toString()

        user?.let {userInfo ->
            userInfo.username = username
            userInfo.email = email
            userInfo.phoneNumber = phoneNumber

            if(uploadedImageUri != null){
                var file = File(Uri.parse(uploadedImageUri).toString())
                var reqFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                var body = MultipartBody.Part.createFormData("imageFile", file.name, reqFile)
                userViewmodel.uploadImage(body).observe(viewLifecycleOwner){image ->
                    if(!image.isNullOrEmpty()){
                        userInfo.profileImagePath = image
                        userViewmodel.updateUserInfo(userInfo).observe(viewLifecycleOwner){
                            binding.profileUpdateProgressbar.isVisible = false
                            if(it){
                                preference[AccountState.PROFILE_IMAGE] = image
                                (requireActivity() as MainActivity).showSnacbar("Profile updated successfully")
                                findNavController().popBackStack()
                            }
                            else Toast.makeText(requireContext() , "Error occurred. Unable to update profile" , Toast.LENGTH_LONG).show()
                        }
                    }
                    else{
                        binding.profileUpdateProgressbar.isVisible = false
                        Toast.makeText(requireContext() , "Something went wrong. unable to update profile information" , Toast.LENGTH_LONG).show()
                    }
                }
            }
            else{
                userViewmodel.updateUserInfo(userInfo).observe(viewLifecycleOwner){
                    binding.profileUpdateProgressbar.isVisible = false
                    if(it){
                        (requireActivity() as MainActivity).showSnacbar("Profile updated successfully")
                        findNavController().popBackStack()
                    }
                    else Toast.makeText(requireContext() , "Error occurred. Unable to update profile" , Toast.LENGTH_LONG).show()
                }
            }
            uploadedImageUri?.let {


            }
        }

    }


    suspend fun createCopyAndReturnRealPath(context: Context, uri: Uri): String? {
        val contentResolver = context.contentResolver

        // Create file path inside app's data dir
        val filePath = (context.applicationInfo.dataDir + File.separator
                + System.currentTimeMillis())
        val file = File(filePath)
        try {
            withContext(Dispatchers.IO){
                val inputStream = contentResolver.openInputStream(uri)
                val outputStream: OutputStream = FileOutputStream(file)
                val buf = ByteArray(1024)
                var len: Int
                inputStream?.let {input ->
                    while (inputStream?.read(buf).also { len = it } > 0)
                        outputStream.write(buf, 0, len)

                    outputStream.close()
                    inputStream.close()
                }
            }

        } catch (error : Throwable) {
            return null
        }
        return file.absolutePath
    }



    fun getImagePat(uri : Uri ) : String?{
        val ins = context?.contentResolver?.openInputStream(uri)
        val file = File(context?.filesDir, "${uri.toString()}.jpg")
        val fileOutputStream = FileOutputStream(file)
        ins?.copyTo(fileOutputStream)
        ins?.close()
        fileOutputStream.close()
        val absolutePath = file.absolutePath
        return absolutePath


//        var columnIndex = arrayOf(MediaStore.Images.Media._ID)
//        var cursor = context?.contentResolver?.query(uri , columnIndex , null , null , null)
//        if(cursor != null){
//            var columnIndexId = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
//        }
//        cursor?.moveToFirst()
//        var docId = cursor?.getString(0)
//        docId = docId?.substring(docId.lastIndexOf(":")+1)
//        cursor?.close()
//        cursor = requireContext().contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null , MediaStore.Images.Media._ID+" = ? " , arrayOf(docId) , null)
//        cursor?.let { cursor ->
//            cursor.moveToFirst()
//            var path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
//            cursor.close()
//            return path
//        }
//        return null

    }


}