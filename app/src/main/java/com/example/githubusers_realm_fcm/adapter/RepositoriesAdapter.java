package com.example.githubusers_realm_fcm.adapter;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.githubusers_realm_fcm.R;
import com.example.githubusers_realm_fcm.db.models.UserRepository;

import java.util.List;

public class RepositoriesAdapter extends RecyclerView.Adapter<RepositoriesAdapter.RepositoriesViewHolder> {

    List<UserRepository> mUserRepositories;

    public RepositoriesAdapter(List<UserRepository> userRepositories) {
        mUserRepositories = userRepositories;
    }

    @NonNull
    @Override
    public RepositoriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.repository_item, parent, false);
        return new RepositoriesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RepositoriesViewHolder holder, int position) {

        holder.repositoryName.setText(mUserRepositories.get(position).getName());
        holder.repositoryUrl.setText(mUserRepositories.get(position).getHtmlUrl());

    }

    @Override
    public int getItemCount() {
        return mUserRepositories.size();
    }

    protected class RepositoriesViewHolder extends RecyclerView.ViewHolder {

        TextView repositoryName, repositoryUrl;

        public RepositoriesViewHolder(@NonNull View itemView) {
            super(itemView);

            repositoryName = itemView.findViewById(R.id.repository_name);
            repositoryUrl = itemView.findViewById(R.id.repository_url);
        }
    }
}
