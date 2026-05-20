package com.appsc.salmo23.biblia;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.appsc.salmo23.R;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> implements Filterable {

    private List<String> bookList;
    private List<String> bookListFull;
    private OnBookClickListener listener;
    private int selectedPosition = -1;
    private static final String LAST_BOOK_KEY = "last_accessed_book";

    public interface OnBookClickListener {
        void onBookClick(String bookName);
    }

    public BookAdapter(List<String> bookList, OnBookClickListener listener) {
        this.bookList = bookList;
        this.bookListFull = new ArrayList<>(bookList);
        this.listener = listener;

        // --- LÓGICA DE MEMÓRIA DE ACESSO ---
        String lastBook = Hawk.get(LAST_BOOK_KEY, null);

        if (lastBook != null) {
            // Procura o índice do último livro acessado na lista atual
            for (int i = 0; i < bookList.size(); i++) {
                if (bookList.get(i).equals(lastBook)) {
                    this.selectedPosition = i;
                    break;
                }
            }
        }

        // Se ainda for -1 (primeiro acesso ou livro não encontrado), seleciona o primeiro (posição 0)
        if (this.selectedPosition == -1 && !bookList.isEmpty()) {
            this.selectedPosition = 0;
        }
    }

    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        String currentBook = bookList.get(position);
        holder.tvBookName.setText(currentBook);

        int progresso = Hawk.get("progress_" + currentBook, 0);
        holder.tvPercentage.setText(progresso + "%");

        Typeface customFont = ResourcesCompat.getFont(holder.itemView.getContext(), R.font.font);

        if (selectedPosition == position) {
            holder.tvBookName.setTextColor(Color.parseColor("#35AEFF"));
            holder.tvBookName.setTypeface(customFont, Typeface.BOLD);
            holder.itemView.setBackgroundColor(Color.parseColor("#15A07844"));
        } else {
            holder.tvBookName.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.grey1));
            holder.tvBookName.setTypeface(customFont, Typeface.NORMAL);
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        holder.itemView.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                String selectedBookName = bookList.get(currentPos);

                // Salva o nome do livro no Hawk para o próximo acesso
                Hawk.put(LAST_BOOK_KEY, selectedBookName);

                int previousSelected = selectedPosition;
                selectedPosition = currentPos;

                notifyItemChanged(previousSelected);
                notifyItemChanged(selectedPosition);

                if (listener != null) {
                    listener.onBookClick(selectedBookName);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    @Override
    public Filter getFilter() {
        return bookFilter;
    }

    private Filter bookFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<String> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(bookListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (String item : bookListFull) {
                    if (item.toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            bookList.clear();
            if (results.values != null) {
                bookList.addAll((List) results.values);
            }

            // Ao filtrar, atualizamos o selectedPosition para manter o destaque no livro correto
            String lastBook = Hawk.get(LAST_BOOK_KEY, null);
            selectedPosition = -1;
            if (lastBook != null) {
                for (int i = 0; i < bookList.size(); i++) {
                    if (bookList.get(i).equals(lastBook)) {
                        selectedPosition = i;
                        break;
                    }
                }
            }
            notifyDataSetChanged();
        }
    };

    static class BookViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookName, tvPercentage;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookName = itemView.findViewById(R.id.tvBookName);
            tvPercentage = itemView.findViewById(R.id.tvPercentage);
        }
    }
}