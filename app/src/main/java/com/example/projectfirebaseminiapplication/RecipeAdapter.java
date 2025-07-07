package com.example.projectfirebaseminiapplication;


import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.projectfirebaseminiapplication.databinding.ItemRecipeBinding;

import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {



    private ArrayList<Recipe> recipeList;
    private final OnRecipeClickListener listener;


    public RecipeAdapter(List<Recipe> recipes, OnRecipeClickListener listener) {
        this.listener = listener;
        if (recipes == null) {
            this.recipeList = new ArrayList<>();
        } else {
            this.recipeList = new ArrayList<>(recipes);

        }
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRecipeBinding binding = ItemRecipeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RecipeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);
        holder.binding.recipeNameTextView.setText(recipe.getName());
        holder.binding.recipeCategoryTextView.setText(recipe.getCategory());
        holder.bind(recipe);

        Glide.with(holder.itemView.getContext())
                .load(recipe.getImageUrl())
                .placeholder(R.drawable.ic_profile)
                .into(holder.binding.recipeImageView);
        holder.itemView.setOnClickListener(v -> listener.onItemClicked(recipe));


    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public void setRecipeList(List<Recipe> newList) {
        if (this.recipeList == null) {
            this.recipeList = new ArrayList<>();
        } else {
            this.recipeList.clear();
        }
        if (newList != null) {
            this.recipeList.addAll(newList);
        }
        notifyDataSetChanged();
    }

    class RecipeViewHolder extends RecyclerView.ViewHolder {

        private final ItemRecipeBinding binding;

        public RecipeViewHolder(@NonNull ItemRecipeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Recipe recipe) {
            binding.recipeNameTextView.setText(recipe.getName());
            binding.recipeCategoryTextView.setText(recipe.getCategory());

            Glide.with(binding.getRoot().getContext())
                    .load(recipe.getImageUrl())
                    .placeholder(R.drawable.ic_profile)
                    .into(binding.recipeImageView);

            binding.getRoot().setOnClickListener(v -> listener.onItemClicked(recipe));
            
        }
    }


}
