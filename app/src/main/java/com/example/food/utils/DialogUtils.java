// DialogUtils.java
package com.example.food.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class DialogUtils {

    public interface OnAmountConfirmed {
        void onConfirmed(double amount);
    }

    public static void showAmountInputDialog(Context context, String title,
                                             Double currentAmount,
                                             OnAmountConfirmed listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText input = new EditText(context);
        if (currentAmount != null) {
            input.setText(String.valueOf(currentAmount));
        }
        input.setHint("请输入重量（克）");
        layout.addView(input);

        builder.setView(layout);

        builder.setPositiveButton("确定", (dialog, which) -> {
            String amountStr = input.getText().toString().trim();
            if (amountStr.isEmpty()) {
                Toast.makeText(context, "请输入食物重量", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                if (listener != null) {
                    listener.onConfirmed(amount);
                }
            } catch (NumberFormatException e) {
                Toast.makeText(context, "请输入有效的数字", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("取消", null);
        builder.show();
    }
}