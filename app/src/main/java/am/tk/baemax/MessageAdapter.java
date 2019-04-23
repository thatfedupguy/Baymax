package am.tk.baemax;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.CustomViewHolder> {

    class CustomViewHolder extends RecyclerView.ViewHolder{
        TextView tv_textMessage;
        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_textMessage = itemView.findViewById(R.id.tv_textMessage);
        }
    }

    List<ResponseMessage> responseMessageList;

    public MessageAdapter(List<ResponseMessage> responseMessageList) {
        this.responseMessageList = responseMessageList;
    }
    @Override
    public int getItemViewType(int position) {
        if(responseMessageList.get(position).isUser){
            return R.layout.user_bubble;
        }
        return R.layout.baemax_bubble;
    }
    @NonNull
    @Override
    public MessageAdapter.CustomViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new CustomViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(i,viewGroup,false));
    }
    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.CustomViewHolder customViewHolder, int i) {
        customViewHolder.tv_textMessage.setText(responseMessageList.get(i).getMessage());
    }
    @Override
    public int getItemCount() {
        return responseMessageList.size();
    }
}
