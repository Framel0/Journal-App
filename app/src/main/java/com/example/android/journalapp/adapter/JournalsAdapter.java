package com.example.android.journalapp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.example.android.journalapp.R;
import com.example.android.journalapp.model.Journal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class JournalsAdapter extends RecyclerView.Adapter<JournalsAdapter.MyViewHolder> implements Filterable {

    private Context mContext;
    private List<Journal> journalList;
    private ProviderFilter providerFilter;

    public JournalsAdapter(Context mContext, List<Journal> journals) {
        this.mContext = mContext;
        this.journalList = journals;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.journal_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Journal journal = journalList.get(position);

        holder.journalTitle.setText(journal.getJournalTitle());

        holder.journalText.setText(journal.getJournalText());

        holder.mark.setBackgroundResource(R.color.colorAccent);

        // Formatting and displaying timestamp
        holder.date.setText(formatDate(journal.getDateStamp()));

        holder.time.setText(journal.getTimeStamp());

    }

    @Override
    public int getItemCount() {
        return journalList.size();
    }

    /**
     * Formatting timestamp to `MMM d` format
     * Input: 2018-02-21 00:15:42
     * Output: Feb 26
     */
    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK);
            Date date = fmt.parse(dateStr);
            SimpleDateFormat fmtOut = new SimpleDateFormat("MMM \n d", Locale.UK);
            return fmtOut.format(date);
        } catch (ParseException ignored) {

        }

        return "";
    }


    @Override
    public Filter getFilter() {
        if (providerFilter == null)
            providerFilter = new ProviderFilter(this, journalList);
        return providerFilter;
    }

    private static class ProviderFilter extends Filter {

        private final JournalsAdapter brandsAdapter;
        private final List<Journal> originalList;
        private final List<Journal> filteredList;

        ProviderFilter(JournalsAdapter brandsAdapter, List<Journal> originalList) {
            this.brandsAdapter = brandsAdapter;
            this.originalList = new LinkedList<>(originalList);
            this.filteredList = new ArrayList<>();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            filteredList.clear();
            final FilterResults results = new FilterResults();

            if (constraint.length() == 0) {
                filteredList.addAll(originalList);
            } else {
                final String filterPattern = constraint.toString().toLowerCase().trim();

                for (final Journal journal : originalList) {
                    if (journal.getJournalTitle().toLowerCase().contains(filterPattern) || journal.getJournalText().toLowerCase().contains(filterPattern)) {
                        filteredList.add(journal);
                    }
                }
            }
            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            brandsAdapter.journalList.clear();
            brandsAdapter.journalList.addAll((List<Journal>) results.values);
            brandsAdapter.notifyDataSetChanged();
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView journalTitle;
        TextView journalText;
        View mark;
        TextView time;
        TextView date;

        MyViewHolder(View itemView) {
            super(itemView);

            journalTitle = itemView.findViewById(R.id.text_journal_title);
            journalText = itemView.findViewById(R.id.text_journal_text);
            mark = itemView.findViewById(R.id.view_mark);
            time = itemView.findViewById(R.id.text_journal_time);
            date = itemView.findViewById(R.id.text_journal_date);
        }
    }


}
