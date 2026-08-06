package com.example.jobfinderapp.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.jobfinderapp.database.DBHelper;
import com.example.jobfinderapp.models.User;
import com.example.jobfinderapp.utils.UserSession;

public class ProjectActivity extends AppCompatActivity {
 private DBHelper db;private int userId;private com.example.jobfinderapp.adapters.ProjectAdapter adapter;
 @Override public void onCreate(Bundle state){super.onCreate(state);UserSession session=new UserSession(this);db=DBHelper.getInstance(this);User user=session.isLoggedIn()?db.getUserById(session.getUserId()):null;if(user==null||!DBHelper.ROLE_CANDIDATE.equals(user.getRole())||!DBHelper.STATUS_ACTIVE.equals(user.getStatus())){session.clear();startActivity(new android.content.Intent(this,LoginActivity.class).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK|android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK));finish();return;}userId=user.getId();setContentView(com.example.jobfinderapp.R.layout.activity_project);findViewById(com.example.jobfinderapp.R.id.btnProjectBack).setOnClickListener(v->finish());androidx.recyclerview.widget.RecyclerView list=findViewById(com.example.jobfinderapp.R.id.rvProjects);list.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));adapter=new com.example.jobfinderapp.adapters.ProjectAdapter(new com.example.jobfinderapp.adapters.ProjectAdapter.Listener(){public void onEdit(com.example.jobfinderapp.models.Project p){}public void onDelete(com.example.jobfinderapp.models.Project p){if(db.deleteProject(p.getId(),userId))loadProjects();}public void onOpenGithub(com.example.jobfinderapp.models.Project p){}public void onOpenDemo(com.example.jobfinderapp.models.Project p){}});list.setAdapter(adapter);loadProjects();}
 private void loadProjects(){adapter.update(db.getProjects(userId));}
}
