package webry.pickerman.redder.adapter;

import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import github.ankushsachdeva.emojicon.EmojiconTextView;
import webry.pickerman.redder.ProfileActivity;
import webry.pickerman.redder.R;
import webry.pickerman.redder.constants.Constants;


import webry.pickerman.redder.model.Chat;
import webry.pickerman.redder.model.Notify;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static webry.pickerman.redder.constants.Constants.NOTIFY_TYPE_PROFILE_COVER_REJECT;
import static webry.pickerman.redder.constants.Constants.NOTIFY_TYPE_PROFILE_PHOTO_REJECT;

public class NotificationsListAdapter extends RecyclerView.Adapter<NotificationsListAdapter.ViewHolder> {

    private Context ctx;
    private List<Notify> items;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {

        void onItemClick(View view, Notify item, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {

        this.mOnItemClickListener = mItemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView title, time;
        public CircleImageView image, online, verified, icon;
        public LinearLayout parent;
        public EmojiconTextView message;

        public ViewHolder(View view) {

            super(view);

            title = (TextView) view.findViewById(R.id.title);
            message = (EmojiconTextView) view.findViewById(R.id.message);
            time = (TextView) view.findViewById(R.id.time);
            image = (CircleImageView) view.findViewById(R.id.image);
            parent = (LinearLayout) view.findViewById(R.id.parent);

            online = (CircleImageView) view.findViewById(R.id.online);
            verified = (CircleImageView) view.findViewById(R.id.verified);
            icon = (CircleImageView) view.findViewById(R.id.icon);
        }
    }

    public NotificationsListAdapter(Context mContext, List<Notify> items) {

        this.ctx = mContext;
        this.items = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification_list_row, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final Notify item = items.get(position);

        holder.online.setVisibility(View.GONE);
        holder.verified.setVisibility(View.GONE);

        if (item.getFromUserPhotoUrl().length() > 0) {

            try {

                Glide.with(ctx).load(item.getFromUserPhotoUrl())
                        .transition(withCrossFade())
                        .into(holder.image);

            } catch (Exception e) {

                Log.e("NotifyListAdapter", e.toString());
            }

        } else {

            holder.image.setImageResource(R.drawable.profile_default_photo);
        }

        holder.title.setText(item.getFromUserFullname());

        if (item.getType() == Constants.NOTIFY_TYPE_COMMENT) {

            holder.message.setText(ctx.getText(R.string.label_comment_added));
            holder.icon.setImageResource(R.drawable.notify_comment);

        } else if (item.getType() == Constants.NOTIFY_TYPE_COMMENT_REPLY) {

            holder.message.setText(ctx.getText(R.string.label_comment_reply_added));
            holder.icon.setImageResource(R.drawable.notify_reply);

    } else if (item.getType() == Constants.NOTIFY_TYPE_ITEM_APPROVED) {

            holder.image.setImageResource(R.drawable.def_photo);

            holder.title.setText(R.string.app_name);

            holder.online.setVisibility(View.VISIBLE);
            holder.verified.setVisibility(View.VISIBLE);

            holder.message.setText(ctx.getText(R.string.label_item_approved));
            holder.icon.setImageResource(R.drawable.notify_approved);

        } else if (item.getType() == NOTIFY_TYPE_PROFILE_PHOTO_REJECT) {

            holder.image.setImageResource(R.drawable.def_photo);
            holder.title.setText(ctx.getString(R.string.app_name));

            holder.online.setVisibility(View.VISIBLE);
            holder.verified.setVisibility(View.VISIBLE);

            holder.message.setText(String.format(Locale.getDefault(), ctx.getString(R.string.label_profile_photo_rejected_new), ctx.getString(R.string.app_name)));
            holder.icon.setImageResource(R.drawable.notify_rejected);

        } else if (item.getType() == NOTIFY_TYPE_PROFILE_COVER_REJECT) {

            holder.image.setImageResource(R.drawable.def_photo);
            holder.title.setText(ctx.getString(R.string.app_name));

            holder.online.setVisibility(View.VISIBLE);
            holder.verified.setVisibility(View.VISIBLE);

            holder.message.setText(String.format(Locale.getDefault(), ctx.getString(R.string.label_profile_cover_rejected_new), ctx.getString(R.string.app_name)));
            holder.icon.setImageResource(R.drawable.notify_rejected);

        } else {

            holder.image.setImageResource(R.drawable.def_photo);

            holder.title.setText(R.string.app_name);

            holder.online.setVisibility(View.VISIBLE);
            holder.verified.setVisibility(View.VISIBLE);

            holder.message.setText(ctx.getText(R.string.label_item_rejected));
            holder.icon.setImageResource(R.drawable.notify_rejected);
        }

        holder.time.setText(item.getTimeAgo());

        holder.parent.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mOnItemClickListener != null) {

                    mOnItemClickListener.onItemClick(v, items.get(position), position);
                }
            }
        });

        holder.image.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Notify item = items.get(position);

                if (item.getFromUserId() != 0) {

                    Intent intent = new Intent(ctx, ProfileActivity.class);
                    intent.putExtra("profileId", item.getFromUserId());
                    ctx.startActivity(intent);
                }
            }
        });
    }

    public Notify getItem(int position) {

        return items.get(position);
    }

    @Override
    public int getItemCount() {

        return items.size();
    }

    public interface OnClickListener {

        void onItemClick(View view, Chat item, int pos);
    }
}