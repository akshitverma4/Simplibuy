package com.example.simplibuy.activties

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.simplibuy.database.ShoppingDatabase
import com.example.simplibuy.R
import com.example.simplibuy.database.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import kotlinx.android.synthetic.main.fragment_scan.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.dm7.barcodescanner.zxing.ZXingScannerView

class ZxingScanner :AppCompatActivity(), ZXingScannerView.ResultHandler {
    private val personCollectionRef = Firebase.firestore.collection("products")
    lateinit var viewModel: ShoppingViewModel
    lateinit var adapter: ShoppingItemAdapter
   // private var mScannerView: ZXingScannerView? = null
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)

        //mScannerView = ZXingScannerView(this)
        setContentView(R.layout.zxing)
       val database = ShoppingDatabase(this@ZxingScanner)
       val repository =
           ShoppingRepository(database)
       val factory =
           ShoppingViewModelFactory(repository)

       viewModel = ViewModelProvider(this, factory).get(ShoppingViewModel::class.java)
       adapter =
           ShoppingItemAdapter(listOf(), viewModel)
       shopping_cart_recycler.adapter = adapter
       shopping_cart_recycler.layoutManager = LinearLayoutManager(this)
      // val total = viewModel.getSubTotal().toString()
//textView.text = total
       viewModel.getAllShoppingItems().observe(this, Observer {
           adapter.items = it
           adapter.notifyDataSetChanged()
        val myformat: MutableList<BarcodeFormat> = ArrayList()
        myformat.add(BarcodeFormat.EAN_13)

        z_xing_scanner!!.setFormats(myformat)


        // Programmatically initialize the scanner view
        // Set the scanner view as the content view
    })
   }

    public override fun onResume() {
        super.onResume()
        z_xing_scanner!!.setResultHandler(this)
        z_xing_scanner!!.startCamera()
    }

    public override fun onPause() {
        super.onPause()
        z_xing_scanner!!.stopCamera()
    }

    override fun handleResult(rawResult: Result) {
        Log.v("YYYY", rawResult.text)
        Log.v(
            "YYYY",
            rawResult.barcodeFormat.toString()
        )

        //saveDataToFirebase(rawResult.text, rawResult.barcodeFormat.toString())

        // mScannerView!!.resumeCameraPreview(this)

        //createDialog(rawResult.toString())
        retrievePersons(rawResult)

    }
    private fun createDialog(boards : Product?){
        val builder= AlertDialog.Builder(this)
        builder.setMessage("Name:"+boards?.Name+"                                           "+"" +
                "Price:"+"${boards?.Price}rs"+"                                                 "+
                "Weight:"+"${boards?.Weight}wg")
        builder.setPositiveButton("ok"){ dialogInterface: android.content.DialogInterface, _: Int ->
            val names = boards?.Name
            val price = boards?.Price
            val weight = boards?.Weight
            val items = Product(names!!,price!!,weight!!)
            viewModel.upsert(items)
            z_xing_scanner!!.resumeCameraPreview(this) }
        val alertDialog: AlertDialog =builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
    private fun retrievePersons(rawResult: Result) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val querySnapshot = personCollectionRef.document("$rawResult")
            querySnapshot.get()

                .addOnSuccessListener { document ->
                    if (document != null) {
                        //Toast.makeText(this@ZxingScanner,"DocumentSnapshot data: ${document.data}", Toast.LENGTH_SHORT).show()
                        val boards = document.toObject(Product::class.java)
                        createDialog(boards)

                    } else {

                    }
                }
                .addOnFailureListener {
                }
            withContext(Dispatchers.Main) {

            }
        }

        catch(e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@ZxingScanner, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
    /*private fun saveDataToFirebase(barcodeData: String, barcodeType: String) {
        val ref = FirebaseDatabase.getInstance().getReference("barcode")
        val barcodeId = ref.push().key

        val barcode = DataModel(barcodeId!!, barcodeData, barcodeType)

        ref.child(barcodeId).setValue(barcode).addOnCompleteListener(OnCompleteListener {
            Toast.makeText(this, "Save Data !!", Toast.LENGTH_SHORT).show()

            finish()
        })
    }*/


}


