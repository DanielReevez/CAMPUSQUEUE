package com.campusqueue.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.campusqueue.app.model.ServiceOffice;

import java.util.List;

public class ServiceAdapter extends ArrayAdapter<ServiceOffice> {

    private static class ViewHolder {
        TextView tvServiceName;
        TextView tvLocation;
        TextView tvWaiting;
        TextView tvWaitTime;
        TextView tvStatus;
    }

    public ServiceAdapter(Context context, List<ServiceOffice> items) {
        super(context, 0, items);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_service, parent, false);
            holder = new ViewHolder();
            holder.tvServiceName = convertView.findViewById(R.id.tvServiceName);
            holder.tvLocation = convertView.findViewById(R.id.tvLocation);
            holder.tvWaiting = convertView.findViewById(R.id.tvWaiting);
            holder.tvWaitTime = convertView.findViewById(R.id.tvWaitTime);
            holder.tvStatus = convertView.findViewById(R.id.tvStatus);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ServiceOffice s = getItem(position);
        if (s != null) {
            holder.tvServiceName.setText(s.getName());
            holder.tvLocation.setText(s.getLocation());
            holder.tvWaiting.setText(getContext().getString(R.string.service_waiting, s.getWaitingCount()));
            holder.tvWaitTime.setText(getContext().getString(R.string.service_wait_time, s.getWaitingCount() * s.getAvgServiceTimeMins()));

            boolean open = "Open".equalsIgnoreCase(s.getStatus());
            holder.tvStatus.setText(open ? R.string.status_open : R.string.status_paused);
            holder.tvStatus.setTextColor(ContextCompat.getColor(getContext(), open ? R.color.cq_green : R.color.cq_warning));
        }

        return convertView;
    }
}
