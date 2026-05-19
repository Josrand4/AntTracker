package com.example.antracker.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.antracker.R;
import java.util.Calendar;

public class MonthYearPickerDialog extends DialogFragment {

    public interface OnDateSetListener {
        void onDateSet(int year, int month);
    }

    private OnDateSetListener listener;
    private int initialYear;
    private int initialMonth;

    private static final String[] MONTHS = {
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    public static MonthYearPickerDialog newInstance(int year, int month) {
        MonthYearPickerDialog dialog = new MonthYearPickerDialog();
        dialog.initialYear = year;
        dialog.initialMonth = month;
        return dialog;
    }

    public void setListener(OnDateSetListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_month_year_picker, null);
        
        NumberPicker pickerMes = view.findViewById(R.id.picker_mes);
        NumberPicker pickerAnio = view.findViewById(R.id.picker_anio);
        
        // Configurar picker de mes
        pickerMes.setMinValue(0);
        pickerMes.setMaxValue(11);
        pickerMes.setDisplayedValues(MONTHS);
        pickerMes.setValue(initialMonth);
        
        // Configurar picker de año
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        pickerAnio.setMinValue(currentYear - 5);
        pickerAnio.setMaxValue(currentYear + 1);
        pickerAnio.setValue(initialYear);
        
        builder.setView(view)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    if (listener != null) {
                        listener.onDateSet(pickerAnio.getValue(), pickerMes.getValue());
                    }
                })
                .setNegativeButton("Cancelar", null);
        
        return builder.create();
    }
}
