package com.example.jobfinderapp.adapters;
import android.view.*;import android.widget.TextView;import androidx.annotation.NonNull;import androidx.recyclerview.widget.RecyclerView;import com.example.jobfinderapp.R;import java.util.List;import java.util.Map;
public class AdminJobAdapter extends RecyclerView.Adapter<AdminJobAdapter.H>{
 public interface L{void click(Map<String,Object> row);} private final List<Map<String,Object>> data;private final L listener;
 public AdminJobAdapter(List<Map<String,Object>> data,L listener){this.data=data;this.listener=listener;}
 @NonNull public H onCreateViewHolder(@NonNull ViewGroup parent,int type){return new H(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_job,parent,false));}
 public void onBindViewHolder(@NonNull H h,int p){Map<String,Object> r=data.get(p);h.title.setText(String.valueOf(r.get("title")));h.info.setText(String.valueOf(r.get("company_name"))+"\n"+String.valueOf(r.get("location"))+" · "+String.valueOf(r.get("status")));h.itemView.setOnClickListener(v->listener.click(r));}
 public int getItemCount(){return data.size();}
 static class H extends RecyclerView.ViewHolder{TextView title,info;H(View v){super(v);title=v.findViewById(R.id.tvAdminJobTitle);info=v.findViewById(R.id.tvAdminJobInfo);}}
}
