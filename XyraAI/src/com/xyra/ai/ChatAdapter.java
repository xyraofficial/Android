package com.xyra.ai;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends BaseAdapter {
    
    private static final int TYPE_USER = 0;
    private static final int TYPE_AI = 1;
    
    private Context context;
    private List<Message> messages;
    private LayoutInflater inflater;
    private SimpleDateFormat timeFormat;
    private MessageRenderer messageRenderer;
    private TypingAnimator typingAnimator;
    private Handler handler;
    
    private String[] thinkingSymbols = {"🧠", "✨", "💭", "⚡", "🔮", "💫"};
    private int currentSymbolIndex = 0;
    private int[] dotColors = {0xFF6366F1, 0xFF8B5CF6, 0xFFA855F7, 0xFFEC4899};
    
    public ChatAdapter(Context context) {
        this.context = context;
        this.messages = new ArrayList<Message>();
        this.inflater = LayoutInflater.from(context);
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        this.messageRenderer = new MessageRenderer(context);
        this.typingAnimator = new TypingAnimator();
        this.handler = new Handler(Looper.getMainLooper());
    }
    
    @Override
    public int getCount() {
        return messages.size();
    }
    
    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }
    
    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public int getViewTypeCount() {
        return 2;
    }
    
    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getType();
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message message = messages.get(position);
        int viewType = getItemViewType(position);
        
        if (viewType == TYPE_USER) {
            return getUserView(message, convertView, parent);
        } else {
            return getAIView(message, convertView, parent);
        }
    }
    
    private View getUserView(Message message, View convertView, ViewGroup parent) {
        UserViewHolder holder;
        
        if (convertView == null || convertView.getTag() == null || !(convertView.getTag() instanceof UserViewHolder)) {
            convertView = inflater.inflate(R.layout.item_message_user, parent, false);
            holder = new UserViewHolder();
            holder.tvMessage = (TextView) convertView.findViewById(R.id.tvMessage);
            holder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
            holder.ivUserImage = (ImageView) convertView.findViewById(R.id.ivUserImage);
            convertView.setTag(holder);
        } else {
            holder = (UserViewHolder) convertView.getTag();
        }
        
        if (message.hasImage()) {
            holder.ivUserImage.setVisibility(View.VISIBLE);
            
            if (message.getImageBitmap() != null) {
                holder.ivUserImage.setImageBitmap(message.getImageBitmap());
            } else if (message.getImageBase64() != null) {
                try {
                    byte[] decodedBytes = Base64.decode(message.getImageBase64(), Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    holder.ivUserImage.setImageBitmap(bitmap);
                    message.setImageBitmap(bitmap);
                } catch (Exception e) {
                    holder.ivUserImage.setVisibility(View.GONE);
                }
            }
            
            String content = message.getContent();
            if (content.startsWith("[Gambar] ")) {
                content = content.substring(9);
            }
            if (content.isEmpty() || content.equals("Analisis gambar ini")) {
                holder.tvMessage.setVisibility(View.GONE);
            } else {
                holder.tvMessage.setVisibility(View.VISIBLE);
                holder.tvMessage.setText(content);
            }
        } else {
            holder.ivUserImage.setVisibility(View.GONE);
            holder.tvMessage.setVisibility(View.VISIBLE);
            holder.tvMessage.setText(message.getContent());
        }
        
        holder.tvTime.setText(timeFormat.format(new Date(message.getTimestamp())));
        
        return convertView;
    }
    
    private View getAIView(Message message, View convertView, ViewGroup parent) {
        AIViewHolder holder;
        
        if (convertView == null || convertView.getTag() == null || !(convertView.getTag() instanceof AIViewHolder)) {
            convertView = inflater.inflate(R.layout.item_message_ai, parent, false);
            holder = new AIViewHolder();
            holder.messageContainer = (LinearLayout) convertView.findViewById(R.id.messageContainer);
            holder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
            convertView.setTag(holder);
        } else {
            holder = (AIViewHolder) convertView.getTag();
        }
        
        String content = message.getContent();
        
        if (isThinkingMessage(content)) {
            showThinkingAnimation(holder.messageContainer);
        } else {
            messageRenderer.renderMessage(content, holder.messageContainer);
        }
        
        holder.tvTime.setText(timeFormat.format(new Date(message.getTimestamp())));
        
        return convertView;
    }
    
    private boolean isThinkingMessage(String content) {
        return content.equals("Thinking...") || 
               content.equals("Sedang berpikir...") ||
               content.equals("thinking");
    }
    
    private void showThinkingAnimation(final LinearLayout container) {
        container.removeAllViews();
        
        LinearLayout thinkingLayout = new LinearLayout(context);
        thinkingLayout.setOrientation(LinearLayout.HORIZONTAL);
        thinkingLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
        
        final TextView symbolView = new TextView(context);
        symbolView.setTextSize(22);
        symbolView.setText(thinkingSymbols[0]);
        thinkingLayout.addView(symbolView);
        
        LinearLayout dotsLayout = new LinearLayout(context);
        dotsLayout.setOrientation(LinearLayout.HORIZONTAL);
        dotsLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams dotsParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        dotsParams.setMargins(16, 0, 16, 0);
        dotsLayout.setLayoutParams(dotsParams);
        
        final View[] dots = new View[4];
        for (int i = 0; i < 4; i++) {
            View dot = new View(context);
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(12, 12);
            dotParams.setMargins(4, 0, 4, 0);
            dot.setLayoutParams(dotParams);
            dot.setBackgroundResource(R.drawable.bg_dot);
            dots[i] = dot;
            dotsLayout.addView(dot);
        }
        thinkingLayout.addView(dotsLayout);
        
        TextView textView = new TextView(context);
        textView.setText("Sedang berpikir");
        textView.setTextColor(0xFFFCD34D);
        textView.setTextSize(14);
        thinkingLayout.addView(textView);
        
        container.addView(thinkingLayout);
        
        startDotsAnimation(dots, symbolView);
    }
    
    private void startDotsAnimation(final View[] dots, final TextView symbolView) {
        final int[] frame = {0};
        
        final Runnable animationRunnable = new Runnable() {
            @Override
            public void run() {
                frame[0]++;
                
                for (int i = 0; i < dots.length; i++) {
                    float phase = (frame[0] + i * 3) % 20;
                    float scale = 0.6f + 0.4f * (float) Math.sin(phase * Math.PI / 10);
                    float translationY = -8f * (float) Math.sin(phase * Math.PI / 10);
                    
                    dots[i].setScaleX(scale);
                    dots[i].setScaleY(scale);
                    dots[i].setTranslationY(translationY);
                    dots[i].setAlpha(0.5f + 0.5f * scale);
                }
                
                if (frame[0] % 12 == 0) {
                    currentSymbolIndex = (currentSymbolIndex + 1) % thinkingSymbols.length;
                    symbolView.setText(thinkingSymbols[currentSymbolIndex]);
                }
                
                handler.postDelayed(this, 80);
            }
        };
        
        handler.post(animationRunnable);
    }
    
    public void addMessage(Message message) {
        messages.add(message);
        notifyDataSetChanged();
    }
    
    public void updateLastMessage(String content) {
        if (!messages.isEmpty()) {
            int lastIndex = messages.size() - 1;
            messages.get(lastIndex).setContent(content);
            notifyDataSetChanged();
        }
    }
    
    public void updateLastMessageWithTyping(final String content, final Runnable onComplete) {
        if (!messages.isEmpty()) {
            final int lastIndex = messages.size() - 1;
            
            typingAnimator.startTyping(content, new TypingAnimator.TypingCallback() {
                @Override
                public void onTextUpdated(String currentText) {
                    messages.get(lastIndex).setContent(currentText);
                    notifyDataSetChanged();
                }
                
                @Override
                public void onTypingComplete(String fullText) {
                    messages.get(lastIndex).setContent(fullText);
                    notifyDataSetChanged();
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            });
        }
    }
    
    public void skipTypingAnimation() {
        typingAnimator.skipToEnd();
    }
    
    public boolean isTyping() {
        return typingAnimator.isAnimating();
    }
    
    public List<Message> getMessages() {
        return messages;
    }
    
    public void clearMessages() {
        messages.clear();
        notifyDataSetChanged();
    }
    
    public void setMessages(List<Message> newMessages) {
        messages.clear();
        messages.addAll(newMessages);
        notifyDataSetChanged();
    }
    
    static class UserViewHolder {
        TextView tvMessage;
        TextView tvTime;
        ImageView ivUserImage;
    }
    
    static class AIViewHolder {
        LinearLayout messageContainer;
        TextView tvTime;
    }
}
