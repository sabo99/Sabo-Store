package com.sabo.sabostore.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sabo.sabostore.Model.SliderModel;
import com.sabo.sabostore.R;
import com.smarteist.autoimageslider.SliderViewAdapter;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SliderAdapter extends SliderViewAdapter<SliderAdapter.ViewHolder> {
    Context context;
    List<SliderModel> sliderModelList;

    public SliderAdapter(Context context, List<SliderModel> sliderModelList) {
        this.context = context;
        this.sliderModelList = sliderModelList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_slide_layout, null));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        SliderModel list = sliderModelList.get(position);

        Picasso.get().load(list.getImage()).into(holder.imgSlider);
        holder.tvDescription.setText(list.getDescription());

//        holder.itemView.setOnClickListener(v -> {
//            EventBus.getDefault().postSticky(new PopularSliderItemClick(sliderModelList.get(position)));
//        });
    }

    @Override
    public int getCount() {
        return sliderModelList.size();
    }

    public class ViewHolder extends SliderViewAdapter.ViewHolder{

        ImageView imgSlider;
        TextView tvDescription;

        public ViewHolder(View itemView) {
            super(itemView);

            imgSlider = itemView.findViewById(R.id.imgSlider);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }
    }
}
