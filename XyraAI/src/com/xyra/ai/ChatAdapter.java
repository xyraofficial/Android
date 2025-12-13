package com.xyra.ai;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
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
    private String currentChatId;
    
    private String[] thinkingSymbols = {"🧠", "✨", "💭", "⚡", "🔮", "💫"};
    private int currentSymbolIndex = 0;
    private int[] dotColors = {0xFF6366F1, 0xFF8B5CF6, 0xFFA855F7, 0xFFEC4899};
    
    private ThemeManager.ThemeColors themeColors;
    
    private TTSService ttsService;
    private BookmarkManager bookmarkManager;
    private ShareService shareService;
    private CodeExecutor codeExecutor;
    
    private ActionButtonCallback actionCallback;
    
    public interface ActionButtonCallback {
        void onTTSClick(Message message);
        void onBookmarkClick(Message message, boolean isNowBookmarked);
        void onShareClick(Message message);
        void onCopyClick(Message message);
        void onRunCodeClick(Message message, String code, String language);
    }
    
    public ChatAdapter(Context context) {
        this.context = context;
        this.messages = new ArrayList<Message>();
        this.inflater = LayoutInflater.from(context);
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        this.messageRenderer = new MessageRenderer(context);
        this.typingAnimator = new TypingAnimator();
        this.handler = new Handler(Looper.getMainLooper());
        this.currentChatId = null;
        this.themeColors = ThemeManager.getThemeColors(context);
    }
    
    public void setServices(TTSService ttsService, BookmarkManager bookmarkManager, 
                           ShareService shareService, CodeExecutor codeExecutor) {
        this.ttsService = ttsService;
        this.bookmarkManager = bookmarkManager;
        this.shareService = shareService;
        this.codeExecutor = codeExecutor;
    }
    
    public void setActionCallback(ActionButtonCallback callback) {
        this.actionCallback = callback;
    }
    
    public void setThemeColors(ThemeManager.ThemeColors colors) {
        this.themeColors = colors;
        notifyDataSetChanged();
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
            return getAIView(message, convertView, parent, position);
        }
    }
    
    private View getUserView(final Message message, View convertView, ViewGroup parent) {
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
            
            holder.ivUserImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (message.getImageBitmap() != null && context instanceof MainActivity) {
                        ((MainActivity) context).showFullscreenImage(message.getImageBitmap());
                    }
                }
            });
            
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
        
        if (themeColors != null) {
            holder.tvMessage.setTextColor(themeColors.isDark ? 0xFFECECF1 : 0xFFFFFFFF);
            holder.tvTime.setTextColor(themeColors.isDark ? 0xFFCBD5E1 : 0xFFE0E0E0);
            View bubbleContainer = convertView.findViewById(R.id.bubbleContainer);
            if (bubbleContainer != null) {
                GradientDrawable userBubbleBg = new GradientDrawable();
                userBubbleBg.setShape(GradientDrawable.RECTANGLE);
                userBubbleBg.setColor(themeColors.userBubble);
                userBubbleBg.setCornerRadii(new float[]{48, 48, 48, 48, 12, 12, 48, 48});
                bubbleContainer.setBackground(userBubbleBg);
            }
        }
        
        return convertView;
    }
    
    private View getAIView(final Message message, View convertView, ViewGroup parent, final int position) {
        AIViewHolder holder;
        
        if (convertView == null || convertView.getTag() == null || !(convertView.getTag() instanceof AIViewHolder)) {
            convertView = inflater.inflate(R.layout.item_message_ai, parent, false);
            holder = new AIViewHolder();
            holder.messageContainer = (LinearLayout) convertView.findViewById(R.id.messageContainer);
            holder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
            holder.actionButtonsContainer = (LinearLayout) convertView.findViewById(R.id.actionButtonsContainer);
            holder.btnTTS = (ImageButton) convertView.findViewById(R.id.btnTTS);
            holder.btnBookmark = (ImageButton) convertView.findViewById(R.id.btnBookmark);
            holder.btnShare = (ImageButton) convertView.findViewById(R.id.btnShare);
            holder.btnCopy = (ImageButton) convertView.findViewById(R.id.btnCopy);
            holder.btnRunCode = (ImageButton) convertView.findViewById(R.id.btnRunCode);
            convertView.setTag(holder);
        } else {
            holder = (AIViewHolder) convertView.getTag();
        }
        
        String content = message.getContent();
        
        if (isThinkingMessage(content)) {
            showThinkingAnimation(holder.messageContainer);
            if (holder.actionButtonsContainer != null) {
                holder.actionButtonsContainer.setVisibility(View.GONE);
            }
        } else {
            messageRenderer.renderMessage(content, holder.messageContainer);
            if (holder.actionButtonsContainer != null) {
                holder.actionButtonsContainer.setVisibility(View.VISIBLE);
            }
            setupActionButtons(holder, message, position);
        }
        
        holder.tvTime.setText(timeFormat.format(new Date(message.getTimestamp())));
        
        if (themeColors != null) {
            holder.tvTime.setTextColor(themeColors.textSecondary);
            View aiBubbleContainer = convertView.findViewById(R.id.aiBubbleContainer);
            if (aiBubbleContainer != null) {
                GradientDrawable aiBubbleBg = new GradientDrawable();
                aiBubbleBg.setShape(GradientDrawable.RECTANGLE);
                aiBubbleBg.setColor(themeColors.aiBubble);
                aiBubbleBg.setCornerRadii(new float[]{12, 12, 48, 48, 48, 48, 48, 48});
                aiBubbleContainer.setBackground(aiBubbleBg);
            }
        }
        
        return convertView;
    }
    
    private void setupActionButtons(final AIViewHolder holder, final Message message, final int position) {
        final String content = message.getContent();
        
        if (holder.btnTTS != null) {
            holder.btnTTS.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ttsService != null) {
                        if (ttsService.isSpeaking()) {
                            ttsService.stop();
                            holder.btnTTS.setAlpha(0.7f);
                        } else {
                            ttsService.autoDetectLanguage(content);
                            ttsService.speak(content);
                            holder.btnTTS.setAlpha(1.0f);
                        }
                    }
                    if (actionCallback != null) {
                        actionCallback.onTTSClick(message);
                    }
                }
            });
        }
        
        if (holder.btnBookmark != null) {
            final boolean isBookmarked = bookmarkManager != null && 
                                         bookmarkManager.isBookmarked(message, currentChatId);
            holder.btnBookmark.setImageResource(isBookmarked ? 
                R.drawable.ic_bookmark_filled : R.drawable.ic_bookmark);
            holder.btnBookmark.setAlpha(isBookmarked ? 1.0f : 0.7f);
            
            holder.btnBookmark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (bookmarkManager != null) {
                        bookmarkManager.toggleBookmark(message, currentChatId);
                        boolean nowBookmarked = bookmarkManager.isBookmarked(message, currentChatId);
                        holder.btnBookmark.setImageResource(nowBookmarked ? 
                            R.drawable.ic_bookmark_filled : R.drawable.ic_bookmark);
                        holder.btnBookmark.setAlpha(nowBookmarked ? 1.0f : 0.7f);
                        
                        Toast.makeText(context, 
                            nowBookmarked ? "Pesan ditandai" : "Tanda dihapus", 
                            Toast.LENGTH_SHORT).show();
                        
                        if (actionCallback != null) {
                            actionCallback.onBookmarkClick(message, nowBookmarked);
                        }
                    }
                }
            });
        }
        
        if (holder.btnShare != null) {
            holder.btnShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (shareService != null) {
                        shareService.shareText(content);
                    }
                    if (actionCallback != null) {
                        actionCallback.onShareClick(message);
                    }
                }
            });
        }
        
        if (holder.btnCopy != null) {
            holder.btnCopy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) 
                        context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("XyraAI", content);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(context, "Disalin ke clipboard", Toast.LENGTH_SHORT).show();
                    
                    if (actionCallback != null) {
                        actionCallback.onCopyClick(message);
                    }
                }
            });
        }
        
        if (holder.btnRunCode != null) {
            boolean hasCode = codeExecutor != null && codeExecutor.containsExecutableCode(content);
            holder.btnRunCode.setVisibility(hasCode ? View.VISIBLE : View.GONE);
            
            if (hasCode) {
                holder.btnRunCode.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (codeExecutor != null) {
                            String code = codeExecutor.extractCodeFromMessage(content);
                            String language = codeExecutor.extractLanguageFromMessage(content);
                            
                            if (code != null && actionCallback != null) {
                                actionCallback.onRunCodeClick(message, code, language);
                            }
                        }
                    }
                });
            }
        }
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
        thinkingLayout.setPadding(4, 8, 4, 8);
        
        LinearLayout dotsLayout = new LinearLayout(context);
        dotsLayout.setOrientation(LinearLayout.HORIZONTAL);
        dotsLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams dotsParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        dotsParams.setMargins(0, 0, 12, 0);
        dotsLayout.setLayoutParams(dotsParams);
        
        final View[] dots = new View[3];
        int[] dotColorList = {0xFF22C55E, 0xFF10B981, 0xFF059669};
        for (int i = 0; i < 3; i++) {
            View dot = new View(context);
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(10, 10);
            dotParams.setMargins(3, 0, 3, 0);
            dot.setLayoutParams(dotParams);
            
            android.graphics.drawable.GradientDrawable dotBg = new android.graphics.drawable.GradientDrawable();
            dotBg.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            dotBg.setColor(dotColorList[i]);
            dot.setBackground(dotBg);
            
            dots[i] = dot;
            dotsLayout.addView(dot);
        }
        thinkingLayout.addView(dotsLayout);
        
        TextView textView = new TextView(context);
        textView.setText("XyraAI sedang berpikir...");
        textView.setTextColor(0xFF6B7280);
        textView.setTextSize(14);
        thinkingLayout.addView(textView);
        
        container.addView(thinkingLayout);
        
        startDotsAnimation(dots);
    }
    
    private void startDotsAnimation(final View[] dots) {
        final float[] progress = {0f};
        
        final Runnable animationRunnable = new Runnable() {
            @Override
            public void run() {
                progress[0] += 0.08f;
                if (progress[0] > 1f) {
                    progress[0] = 0f;
                }
                
                for (int i = 0; i < dots.length; i++) {
                    float dotPhase = (progress[0] + (i * 0.25f)) % 1f;
                    float bounce = (float) Math.sin(dotPhase * Math.PI * 2);
                    float scale = 0.7f + 0.3f * Math.abs(bounce);
                    float translationY = -5f * bounce;
                    float alpha = 0.6f + 0.4f * Math.abs(bounce);
                    
                    dots[i].setScaleX(scale);
                    dots[i].setScaleY(scale);
                    dots[i].setTranslationY(translationY);
                    dots[i].setAlpha(alpha);
                }
                
                handler.postDelayed(this, 50);
            }
        };
        
        handler.post(animationRunnable);
    }
    
    public void addMessage(Message message) {
        if (currentChatId != null) {
            message.setChatId(currentChatId);
        }
        messages.add(message);
        notifyDataSetChanged();
    }
    
    public void setCurrentChatId(String chatId) {
        this.currentChatId = chatId;
    }
    
    public String getCurrentChatId() {
        return currentChatId;
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
    
    public void updateLastMessageWithTyping(final String content, final String chatId, final Runnable onComplete) {
        if (!messages.isEmpty() && chatId != null && chatId.equals(currentChatId)) {
            final int lastIndex = messages.size() - 1;
            
            typingAnimator.startTyping(content, new TypingAnimator.TypingCallback() {
                @Override
                public void onTextUpdated(String currentText) {
                    if (chatId.equals(currentChatId)) {
                        messages.get(lastIndex).setContent(currentText);
                        notifyDataSetChanged();
                    }
                }
                
                @Override
                public void onTypingComplete(String fullText) {
                    if (chatId.equals(currentChatId)) {
                        messages.get(lastIndex).setContent(fullText);
                        notifyDataSetChanged();
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    }
                }
            });
        }
    }
    
    public void updateLastMessage(String content, String chatId) {
        if (!messages.isEmpty() && chatId != null && chatId.equals(currentChatId)) {
            int lastIndex = messages.size() - 1;
            messages.get(lastIndex).setContent(content);
            notifyDataSetChanged();
        }
    }
    
    public void setMessages(List<Message> newMessages, String chatId) {
        messages.clear();
        messages.addAll(newMessages);
        this.currentChatId = chatId;
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
        LinearLayout actionButtonsContainer;
        ImageButton btnTTS;
        ImageButton btnBookmark;
        ImageButton btnShare;
        ImageButton btnCopy;
        ImageButton btnRunCode;
    }
}
