package com.aether.aegis.ui.components;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.aether.aegis.R;
import com.aether.aegis.data.model.EvidenceEdge;
import com.aether.aegis.data.model.EvidenceNode;
import com.aether.aegis.data.model.Severity;

import java.util.ArrayList;
import java.util.List;

public class EvidenceGraphView extends View {

    public interface OnNodeSelectedListener {
        void onNodeSelected(EvidenceNode node);
    }

    private final List<EvidenceNode> nodes = new ArrayList<>();
    private final List<EvidenceEdge> edges = new ArrayList<>();
    private OnNodeSelectedListener nodeSelectedListener;
    private EvidenceNode selectedNode = null;

    private Paint linePaint;
    private Paint lineThreatPaint;
    private Paint nodeBgPaint;
    private Paint nodeBorderPaint;
    private Paint nodeTextPaint;
    private Paint nodeSubtextPaint;
    private Paint haloPaint;

    private int colorBg;
    private int colorBorder;
    private int colorBorderActive;
    private int colorSafe;
    private int colorWarning;
    private int colorDanger;
    private int colorInfo;
    private int colorTextPrimary;
    private int colorTextSecondary;

    private int colorInfoBg;
    private int colorLineBase;

    private float density;
    private float animationProgress = 1.0f; // 0.0 to 1.0
    private ValueAnimator entranceAnimator;

    public EvidenceGraphView(Context context) {
        super(context);
        init();
    }

    public EvidenceGraphView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EvidenceGraphView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        density = getResources().getDisplayMetrics().density;

        colorBg = ContextCompat.getColor(getContext(), R.color.aegis_card);
        colorBorder = ContextCompat.getColor(getContext(), R.color.aegis_border);
        colorBorderActive = ContextCompat.getColor(getContext(), R.color.aegis_info);
        colorSafe = ContextCompat.getColor(getContext(), R.color.aegis_safe);
        colorWarning = ContextCompat.getColor(getContext(), R.color.aegis_warning);
        colorDanger = ContextCompat.getColor(getContext(), R.color.aegis_danger);
        colorInfo = ContextCompat.getColor(getContext(), R.color.aegis_info);
        colorInfoBg = ContextCompat.getColor(getContext(), R.color.aegis_info_bg);
        colorLineBase = ContextCompat.getColor(getContext(), R.color.aegis_border);
        colorTextPrimary = ContextCompat.getColor(getContext(), R.color.aegis_text_primary);
        colorTextSecondary = ContextCompat.getColor(getContext(), R.color.aegis_text_secondary);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(colorLineBase);
        linePaint.setStrokeWidth(2f * density);
        linePaint.setStyle(Paint.Style.STROKE);

        lineThreatPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lineThreatPaint.setColor(colorDanger);
        lineThreatPaint.setStrokeWidth(2.5f * density);
        lineThreatPaint.setStyle(Paint.Style.STROKE);
        lineThreatPaint.setPathEffect(new DashPathEffect(new float[]{14 * density, 8 * density}, 0));

        nodeBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        nodeBgPaint.setStyle(Paint.Style.FILL);

        nodeBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        nodeBorderPaint.setStyle(Paint.Style.STROKE);
        nodeBorderPaint.setStrokeWidth(1.2f * density);

        haloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        haloPaint.setStyle(Paint.Style.STROKE);
        haloPaint.setStrokeWidth(3.5f * density);

        nodeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        nodeTextPaint.setTextAlign(Paint.Align.CENTER);
        nodeTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        nodeTextPaint.setTextSize(12 * density);

        nodeSubtextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        nodeSubtextPaint.setTextAlign(Paint.Align.CENTER);
        nodeSubtextPaint.setTextSize(10 * density);
        nodeSubtextPaint.setColor(colorTextSecondary);
    }

    public void setGraphData(List<EvidenceNode> newNodes, List<EvidenceEdge> newEdges) {
        nodes.clear();
        edges.clear();
        if (newNodes != null) nodes.addAll(newNodes);
        if (newEdges != null) edges.addAll(newEdges);
        layoutNodes();
        startEntranceAnimation();
    }

    public void setOnNodeSelectedListener(OnNodeSelectedListener listener) {
        this.nodeSelectedListener = listener;
    }

    private void startEntranceAnimation() {
        if (entranceAnimator != null && entranceAnimator.isRunning()) {
            entranceAnimator.cancel();
        }
        animationProgress = 0f;
        entranceAnimator = ValueAnimator.ofFloat(0f, 1f);
        entranceAnimator.setDuration(1200);
        entranceAnimator.setInterpolator(new DecelerateInterpolator(1.6f));
        entranceAnimator.addUpdateListener(animation -> {
            animationProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        entranceAnimator.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        layoutNodes();
    }

    private void layoutNodes() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0 || nodes.isEmpty()) return;

        float centerX = w / 2f;
        float leftX = w * 0.28f;
        float rightX = w * 0.72f;

        float topY = h * 0.18f;
        float midY = h * 0.50f;
        float botY = h * 0.82f;

        for (EvidenceNode node : nodes) {
            switch (node.getType()) {
                case APP:
                    node.x = centerX;
                    node.y = topY;
                    break;
                case PERMISSION:
                    node.x = leftX;
                    node.y = midY;
                    break;
                case DOMAIN:
                    node.x = rightX;
                    node.y = midY;
                    break;
                case THREAT_INTEL:
                    node.x = rightX;
                    node.y = botY;
                    break;
                case RISK:
                    node.x = leftX;
                    node.y = botY;
                    break;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (nodes.isEmpty()) return;

        // Stage 1 (0.00 - 0.25): App Node
        // Stage 2 (0.25 - 0.50): Edges draw outward
        // Stage 3 (0.50 - 0.75): Perm & Domain nodes
        // Stage 4 (0.75 - 1.00): Threat Intel & Risk nodes

        // 1. Draw Edges if progress >= 0.25
        if (animationProgress >= 0.25f) {
            float edgeProgress = Math.min(1.0f, (animationProgress - 0.25f) / 0.5f);
            for (EvidenceEdge edge : edges) {
                EvidenceNode src = findNode(edge.getSourceId());
                EvidenceNode dst = findNode(edge.getTargetId());
                if (src != null && dst != null) {
                    float targetX = src.x + ((dst.x - src.x) * edgeProgress);
                    float targetY = src.y + ((dst.y - src.y) * edgeProgress);

                    Paint p = edge.isThreatPath() ? lineThreatPaint : linePaint;
                    boolean isDimmed = (selectedNode != null && !isEdgeConnected(edge, selectedNode));
                    p.setAlpha(isDimmed ? 45 : 255);
                    canvas.drawLine(src.x, src.y, targetX, targetY, p);
                }
            }
        }

        // 2. Draw Nodes
        RectF rect = new RectF();
        for (EvidenceNode node : nodes) {
            float nodeAlpha = getNodeAlpha(node);
            if (nodeAlpha <= 0f) continue;

            // Dimensions based on hierarchy (Executive cybersecurity aesthetics)
            float halfW;
            float halfH;
            float cornerRadius;

            if (node.getType() == EvidenceNode.NodeType.APP || node.getType() == EvidenceNode.NodeType.RISK) {
                halfW = 82 * density;
                halfH = 30 * density;
                cornerRadius = 10 * density;
            } else {
                halfW = 74 * density;
                halfH = 26 * density;
                cornerRadius = 8 * density;
            }

            rect.set(node.x - halfW, node.y - halfH, node.x + halfW, node.y + halfH);

            boolean isSelected = (node == selectedNode);
            boolean isDimmed = (selectedNode != null && !isSelected && !isNodeConnected(node, selectedNode));
            int effectiveAlpha = (int) (nodeAlpha * (isDimmed ? 50 : 255));

            // Halo for selected or critical
            if (isSelected || (node.getSeverity() == Severity.CRITICAL && !isDimmed)) {
                haloPaint.setColor(getSemanticColor(node.getSeverity()));
                haloPaint.setAlpha(isSelected ? (int)(effectiveAlpha * 0.65f) : (int)(effectiveAlpha * 0.22f));
                canvas.drawRoundRect(rect, cornerRadius + (3 * density), cornerRadius + (3 * density), haloPaint);
            }

            // Node Background
            nodeBgPaint.setColor(isSelected ? colorInfoBg : colorBg);
            nodeBgPaint.setAlpha(effectiveAlpha);
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, nodeBgPaint);

            // Node Border
            int strokeColor = isSelected ? colorBorderActive : getSemanticColor(node.getSeverity());
            nodeBorderPaint.setColor(strokeColor);
            nodeBorderPaint.setStrokeWidth(isSelected ? (2.2f * density) : (1.2f * density));
            nodeBorderPaint.setAlpha(effectiveAlpha);
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, nodeBorderPaint);

            // Node Text
            nodeTextPaint.setColor(colorTextPrimary);
            nodeTextPaint.setAlpha(effectiveAlpha);
            canvas.drawText(node.getLabel(), node.x, node.y - (3 * density), nodeTextPaint);

            // Node Subtitle
            nodeSubtextPaint.setColor(colorTextSecondary);
            nodeSubtextPaint.setAlpha(effectiveAlpha);
            canvas.drawText(node.getSubtitle(), node.x, node.y + (13 * density), nodeSubtextPaint);
        }
    }

    private float getNodeAlpha(EvidenceNode node) {
        switch (node.getType()) {
            case APP:
                return Math.min(1.0f, animationProgress / 0.3f);
            case PERMISSION:
            case DOMAIN:
                if (animationProgress < 0.4f) return 0f;
                return Math.min(1.0f, (animationProgress - 0.4f) / 0.3f);
            case THREAT_INTEL:
            case RISK:
                if (animationProgress < 0.7f) return 0f;
                return Math.min(1.0f, (animationProgress - 0.7f) / 0.3f);
            default:
                return 1.0f;
        }
    }

    private boolean isNodeConnected(EvidenceNode a, EvidenceNode b) {
        if (a == null || b == null) return false;
        for (EvidenceEdge e : edges) {
            if ((e.getSourceId().equals(a.getId()) && e.getTargetId().equals(b.getId())) ||
                    (e.getSourceId().equals(b.getId()) && e.getTargetId().equals(a.getId()))) {
                return true;
            }
        }
        return false;
    }

    private boolean isEdgeConnected(EvidenceEdge e, EvidenceNode node) {
        if (e == null || node == null) return false;
        return e.getSourceId().equals(node.getId()) || e.getTargetId().equals(node.getId());
    }

    private int getSemanticColor(Severity severity) {
        switch (severity) {
            case CRITICAL:
            case HIGH:
                return colorDanger;
            case SUSPICIOUS:
                return colorWarning;
            case SAFE:
            default:
                return colorSafe;
        }
    }

    private EvidenceNode findNode(String id) {
        for (EvidenceNode n : nodes) {
            if (n.getId().equals(id)) return n;
        }
        return null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            float tx = event.getX();
            float ty = event.getY();

            for (EvidenceNode node : nodes) {
                float halfW = (node.getType() == EvidenceNode.NodeType.APP || node.getType() == EvidenceNode.NodeType.RISK) ? 76 * density : 68 * density;
                float halfH = (node.getType() == EvidenceNode.NodeType.APP || node.getType() == EvidenceNode.NodeType.RISK) ? 28 * density : 25 * density;

                if (tx >= node.x - halfW && tx <= node.x + halfW &&
                        ty >= node.y - halfH && ty <= node.y + halfH) {
                    if (selectedNode == node) {
                        selectedNode = null; // Toggle unselect
                    } else {
                        selectedNode = node;
                    }
                    invalidate();
                    if (nodeSelectedListener != null && selectedNode != null) {
                        nodeSelectedListener.onNodeSelected(selectedNode);
                    }
                    return true;
                }
            }
            // Tap on background clears selection
            if (selectedNode != null) {
                selectedNode = null;
                invalidate();
            }
        }
        return true;
    }
}
