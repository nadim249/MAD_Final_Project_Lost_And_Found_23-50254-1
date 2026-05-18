package com.example.lostandfound

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class ReportsAdapter(
    private var reports: List<ReportModel>,
    private val onAction: (ReportModel, String) -> Unit
) : RecyclerView.Adapter<ReportsAdapter.ReportViewHolder>() {

    class ReportViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvReportedItem: TextView = view.findViewById(R.id.tvReportedItem)
        val tvReportStatus: TextView = view.findViewById(R.id.tvReportStatus)
        val tvReporterEmail: TextView = view.findViewById(R.id.tvReporterEmail)
        val tvReason: TextView = view.findViewById(R.id.tvReason)
        val btnViewItem: MaterialButton = view.findViewById(R.id.btnViewItem)
        val btnDismiss: MaterialButton = view.findViewById(R.id.btnDismiss)
        val btnResolveReport: MaterialButton = view.findViewById(R.id.btnResolveReport)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_report_row, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reports[position]
        holder.tvReportedItem.text = "Reported: ${report.itemTitle}"
        holder.tvReportStatus.text = report.status
        holder.tvReporterEmail.text = "By: ${report.reportedByEmail}"
        holder.tvReason.text = "Reason: ${report.reason}"

        holder.btnViewItem.setOnClickListener { onAction(report, "VIEW") }
        holder.btnDismiss.setOnClickListener { onAction(report, "DISMISS") }
        holder.btnResolveReport.setOnClickListener { onAction(report, "DELETE_ITEM") }
    }

    override fun getItemCount() = reports.size

    fun updateData(newReports: List<ReportModel>) {
        reports = newReports
        notifyDataSetChanged()
    }
}