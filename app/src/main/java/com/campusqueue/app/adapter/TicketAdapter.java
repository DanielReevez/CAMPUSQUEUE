package com.campusqueue.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.campusqueue.app.R;
import com.campusqueue.app.model.Ticket;
import com.campusqueue.app.model.TicketStatus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TicketAdapter extends ArrayAdapter<Ticket> {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault());

    private static class ViewHolder {
        TextView tvTicketNumber;
        TextView tvStatus;
        TextView tvServiceName;
        TextView tvStudentName;
        TextView tvDate;
        TextView tvReturnReason;
    }

    public TicketAdapter(Context context, List<Ticket> tickets) {
        super(context, 0, tickets);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_ticket, parent, false);
            holder = new ViewHolder();
            holder.tvTicketNumber = convertView.findViewById(R.id.tvTicketNumber);
            holder.tvStatus = convertView.findViewById(R.id.tvStatus);
            holder.tvServiceName = convertView.findViewById(R.id.tvServiceName);
            holder.tvStudentName = convertView.findViewById(R.id.tvStudentName);
            holder.tvDate = convertView.findViewById(R.id.tvDate);
            holder.tvReturnReason = convertView.findViewById(R.id.tvReturnReason);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Ticket t = getItem(position);
        if (t != null) {
            holder.tvTicketNumber.setText(t.getTicketNumber());
            holder.tvStatus.setText(t.getStatus().getDisplayName());
            holder.tvServiceName.setText(t.getServiceName());
            holder.tvStudentName.setText("Student: " + t.getStudentName());
            holder.tvDate.setText("Created: " + dateFormat.format(new Date(t.getCreatedAt())));

            if (t.getStatus() == TicketStatus.RETURN_REQUIRED && t.getReturnReason() != null) {
                holder.tvReturnReason.setVisibility(View.VISIBLE);
                holder.tvReturnReason.setText("Reason: " + t.getReturnReason());
            } else {
                holder.tvReturnReason.setVisibility(View.GONE);
            }
        }

        return convertView;
    }
}
