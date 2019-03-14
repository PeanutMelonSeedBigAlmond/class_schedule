package com.wp.csmu.classschedule.view.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wp.csmu.classschedule.R;
import com.wp.csmu.classschedule.view.bean.Score;

import java.util.List;

public class ScoreRecyclerAdapter extends RecyclerView.Adapter<ScoreRecyclerAdapter.ViewHolder> {
    private List<Score>data;
    private OnClickListener listener;

    public ScoreRecyclerAdapter(List<Score>data,@NonNull OnClickListener listener) {
        this.data = data;
        this.listener=listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.score_recyclerview_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.subjectName.setText(data.get(position).getSubject());
        holder.subjectEnglishName.setText(data.get(position).getSubjectEnglish());
        holder.score.setText(String.valueOf(data.get(position).getScore()));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(v,holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return data==null?0:data.size();
    }

    public void updateData(List<Score>newData){
        this.data=newData;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView subjectName;
        TextView subjectEnglishName;
        TextView score;
        ViewHolder(View itemView) {
            super(itemView);
            subjectName=itemView.findViewById(R.id.scoreRecyclerItemTvSubject);
            subjectEnglishName=itemView.findViewById(R.id.scoreRecyclerItemTvSubjectEnglish);
            score=itemView.findViewById(R.id.scoreRecyclerItemTvScore);
        }
    }

    public interface OnClickListener{
        void onClick(View view, int position);
    }
}
