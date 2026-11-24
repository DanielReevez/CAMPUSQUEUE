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
import com.campusqueue.app.db.DatabaseHelper;
import com.campusqueue.app.model.ServiceOffice;
import com.campusqueue.app.model.User;

import java.util.List;

public class StaffAdapter extends ArrayAdapter<User> {

    private final DatabaseHelper db;

    private static class ViewHolder {
        TextView tvStaffName;
        TextView tvStaffStatus;
        TextView tvStaffEmail;
        TextView tvStaffAssignedOffice;
    }

    public StaffAdapter(Context context, List<User> staffMembers) {
        super(context, 0, staffMembers);
        this.db = DatabaseHelper.getInstance(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_staff, parent, false);
            holder = new ViewHolder();
            holder.tvStaffName = convertView.findViewById(R.id.tvStaffName);
            holder.tvStaffStatus = convertView.findViewById(R.id.tvStaffStatus);
            holder.tvStaffEmail = convertView.findViewById(R.id.tvStaffEmail);
            holder.tvStaffAssignedOffice = convertView.findViewById(R.id.tvStaffAssignedOffice);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        User u = getItem(position);
        if (u != null) {
            String titleName = u.name + (u.position != null && u.position.length() > 0 ? " (" + u.position + ")" : "");
            holder.tvStaffName.setText(titleName);
            holder.tvStaffEmail.setText(u.email);

            boolean active = u.isActive;
            holder.tvStaffStatus.setText(active ? "Active" : "Inactive");
            holder.tvStaffStatus.setTextColor(active ? getContext().getResources().getColor(R.color.cq_green) : getContext().getResources().getColor(R.color.cq_error));

            if (u.role.equalsIgnoreCase("ADMIN") || u.role.equalsIgnoreCase("Administrator")) {
                holder.tvStaffAssignedOffice.setText("Role: Administrator");
            } else {
                List<ServiceOffice> assigned = db.getAssignedServicesForStaff(u.id);
                if (!assigned.isEmpty()) {
                    holder.tvStaffAssignedOffice.setText("Assigned: " + assigned.get(0).getName());
                } else {
                    holder.tvStaffAssignedOffice.setText("Assigned: None");
                }
            }
        }

        return convertView;
    }
}
