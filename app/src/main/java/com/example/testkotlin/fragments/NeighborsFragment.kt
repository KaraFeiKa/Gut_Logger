package com.example.testkotlin.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.testkotlin.Info.InfoNeiborhood
import com.example.testkotlin.Info.ServiceBack
import com.example.testkotlin.MainViewModel
import com.example.testkotlin.databinding.FragmentHeighborsBinding


class NeighborsFragment : Fragment() {
    private lateinit var binding: FragmentHeighborsBinding
    private val model : MainViewModel by activityViewModels()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHeighborsBinding.inflate(inflater, container, false)
        creatHead()
        return binding.root
    }


    enum class Networks {
        LTE,
        UMTS,
        GSM
    }

    override fun onResume() {
        super.onResume()
        creatHead()
    }

    private fun NeighborsUpdate() =with(binding){
        model.neighboursUpdate.observe(viewLifecycleOwner){

        }
    }

    private fun creatHead(){
        binding.tableLayout.removeAllViews()
        var currRow = 0
       var Type =ServiceBack.isNetworksType
        Log.d("type net ", Type)
        if (Type == "4G"){
            context?.let { ctx ->
                val tableRowLte = TableRow(ctx)
                tableRowLte.layoutParams = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
                )

                val textViewList = mutableListOf<TextView>()
                val labels = listOf("PCI", "Earfcn", "Band", "RSSI", "RSRP", "RSRQ", "Ta")

                labels.forEachIndexed { index, label ->
                    val textView = TextView(ctx)
                    textView.textSize = 20f
                    textView.text = "$label   "
                    tableRowLte.addView(textView, index)
                    textViewList.add(textView)
                }

                binding.tableLayout.addView(tableRowLte, currRow)
                currRow++
                networkHeaders = Networks.LTE
            }
        }
        if (Type =="3G"){
            context?.let { ctx ->
                val tableRowUMTS = TableRow(ctx)
                tableRowUMTS.layoutParams = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
                )

                val textViewList = mutableListOf<TextView>()
                val labels = listOf("PSC", "Uarfcn", "dBm")

                labels.forEachIndexed { index, label ->
                    val textView = TextView(ctx)
                    textView.textSize = 20f
                    textView.text = "$label   "
                    tableRowUMTS.addView(textView, index)
                    textViewList.add(textView)
                }

                binding.tableLayout.addView(tableRowUMTS, currRow)
                currRow++
                networkHeaders = Networks.UMTS
            }
        }

        if (Type == "2G"){
            context?.let { ctx ->
                val tableRow = TableRow(ctx)
                tableRow.layoutParams = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
                )

                val textViewList = mutableListOf<TextView>()
                val labels = listOf("LAC", "Cell ID", "ARFCN", "BSIC", "RSSI")

                labels.forEachIndexed { index, label ->
                    val textView = TextView(ctx)
                    textView.textSize = 20f
                    textView.text = "$label   "
                    tableRow.addView(textView, index)
                    textViewList.add(textView)
                }

                binding.tableLayout.addView(tableRow, currRow)
                currRow++
                networkHeaders = Networks.GSM
            }

        }
    }

    private fun createTable(neighbours: ArrayList<InfoNeiborhood>) {
        var childCount = binding.tableLayout.childCount;

        if (childCount > 1) {
            binding.tableLayout.removeViews(1, childCount - 1);
        }

        var currRow = 1


        for (neighbour in neighbours) {
            if (neighbour.Type == "4G" && networkHeaders == Networks.LTE) {
                getContext()?.let { ctx ->
                    val tableRowValues = TableRow(ctx)
                    tableRowValues.layoutParams = TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT
                    )

                    val textViewList = mutableListOf<TextView>()

                    val tvPciVal = TextView(ctx).apply {
                        textSize = 20f
                        text =  neighbour.pci.toString()
                    }
                    tableRowValues.addView(tvPciVal, 0)
                    textViewList.add(tvPciVal)

                    val tvEarfcnVal = TextView(ctx).apply {
                        textSize = 20f
                        text = neighbour.Earfcn.toString()
                    }
                    tableRowValues.addView(tvEarfcnVal, 1)
                    textViewList.add(tvEarfcnVal)


                    val tvBandVal = TextView(ctx).apply {
                        textSize = 20f
                        text = neighbour.band.toString()
                    }
                    tableRowValues.addView(tvBandVal, 2)
                    textViewList.add(tvBandVal)


                    val tvRssiVal = TextView(ctx).apply {
                        textSize = 20f
                        val rssi = neighbour.rssi
                        text = if (rssi != Integer.MAX_VALUE) rssi.toString() else "N/a"
                    }
                    tableRowValues.addView(tvRssiVal, 3)
                    textViewList.add(tvRssiVal)

                    val tvRsrpVal = TextView(ctx).apply {
                        textSize = 20f
                        val rsrp = neighbour.rsrp
                        text = if (rsrp != Integer.MAX_VALUE) rsrp.toString() else "N/a"
                    }
                    tableRowValues.addView(tvRsrpVal, 4)
                    textViewList.add(tvRsrpVal)

                    val tvRsrqVal = TextView(ctx).apply {
                        textSize = 20f
                        val rsrq =neighbour. rsrq
                        text = if (rsrq != Integer.MAX_VALUE) rsrq.toString() else "N/a"
                    }
                    tableRowValues.addView(tvRsrqVal, 5)
                    textViewList.add(tvRsrqVal)

                    val tvTaVal = TextView(ctx).apply {
                        textSize = 20f
                        val ta =neighbour. ta
                        text = if (ta != Integer.MAX_VALUE) {
                            ta.toString()
                        } else {
                            "N/a"
                        }
                    }
                    tableRowValues.addView(tvTaVal, 6)
                    textViewList.add(tvTaVal)

                    binding.tableLayout.addView(tableRowValues, currRow)
                    currRow++
                }

            }

            if (neighbour.Type == "3G" && networkHeaders == Networks.UMTS) {
                getContext()?.let { ctx ->
                    val tableRowValues = TableRow(ctx)
                    tableRowValues.layoutParams = TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT
                    )

                    val textViewList = mutableListOf<TextView>()

                    val PSC = TextView(ctx).apply {
                        textSize = 20f
                        text =  neighbour.psc.toString()
                    }
                    tableRowValues.addView(PSC, 0)
                    textViewList.add(PSC)

                    val UARFCN = TextView(ctx).apply {
                        textSize = 20f
                        text = neighbour.Uarfcn.toString()
                    }
                    tableRowValues.addView(UARFCN, 1)
                    textViewList.add(UARFCN)


                    val SS = TextView(ctx).apply {
                        textSize = 20f
                        text = neighbour.ss.toString()
                    }
                    tableRowValues.addView(SS, 2)
                    textViewList.add(SS)



                    binding.tableLayout.addView(tableRowValues, currRow)
                    currRow++
                }
            }


            if (neighbour.Type == "2G" && networkHeaders == Networks.GSM) {
                getContext()?.let { ctx ->
                    val tableRowValues = TableRow(ctx)
                    tableRowValues.layoutParams = TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT
                    )

                    val textViewList = mutableListOf<TextView>()

                    val LAC = TextView(ctx).apply {
                        textSize = 20f
                        if (neighbour.lac == Int.MAX_VALUE){}
                        text =  neighbour.lac.toString()
                    }
                    tableRowValues.addView(LAC, 0)
                    textViewList.add(LAC)

                    val CELLID = TextView(ctx).apply {
                        textSize = 20f
                        text = neighbour.ci.toString()
                    }
                    tableRowValues.addView(CELLID, 1)
                    textViewList.add(CELLID)


                    val ARFCN = TextView(ctx).apply {
                        textSize = 20f
                        text = neighbour.Arfcn.toString()
                    }
                    tableRowValues.addView(ARFCN, 2)
                    textViewList.add(ARFCN)

                    val BSIC = TextView(ctx).apply {
                        textSize = 20f
                        text = neighbour.bsic.toString()
                    }
                    tableRowValues.addView(BSIC, 3)
                    textViewList.add(BSIC)

                    val RSSI = TextView(ctx).apply {
                        textSize = 20f
                        text = neighbour.rssi2g.toString()
                    }
                    tableRowValues.addView(RSSI, 4)
                    textViewList.add(RSSI)



                    binding.tableLayout.addView(tableRowValues, currRow)
                    currRow++
                }
            }


        }

    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerLocReceiver()
        NeighborsUpdate()

    }


    private val receiverN = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, neighbours: Intent?) {
            if (neighbours?.action == ServiceBack.INFO_NEIGHBOURS) {
                val infoNeighbours =
                    neighbours.getSerializableExtra(ServiceBack.INFO_NEIGHBOURS) as ArrayList<InfoNeiborhood>
                Log.d("Info about Neighbours", infoNeighbours.toString())
                createTable(infoNeighbours)
            }
        }
    }

    private fun registerLocReceiver(){
        val locFilterNeibor = IntentFilter(ServiceBack.INFO_NEIGHBOURS)
        LocalBroadcastManager.getInstance(activity as AppCompatActivity).registerReceiver(receiverN, locFilterNeibor)

    }

    companion object {
        @JvmStatic
        fun newInstance() = NeighborsFragment()
        var networkHeaders = Networks.LTE
    }
}

