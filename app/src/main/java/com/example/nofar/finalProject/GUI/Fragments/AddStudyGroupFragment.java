package com.example.nofar.finalProject.GUI.Fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.nofar.finalProject.GUI.Dialogs.ItemDialog;
import com.example.nofar.finalProject.LOGIC.Core.Course;
import com.example.nofar.finalProject.LOGIC.Core.StudyGroup;
import com.example.nofar.finalProject.LOGIC.Core.User;
import com.example.nofar.finalProject.LOGIC.Enums.Term;
import com.example.nofar.finalProject.LOGIC.Interfaces.AddExamDialogListener;
import com.example.nofar.finalProject.LOGIC.Interfaces.AddStudyGroupDialogListener;
import com.example.nofar.finalProject.LOGIC.Interfaces.DismissListener;
import com.example.nofar.finalProject.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddStudyGroupFragment extends android.support.v4.app.Fragment
{
    private final String PATTERN = "dd/MM/yy";
    private String opt;
    private StudyGroup studyGroup;
    private TextView toolbarTitleText;
    private TextView pickClassText;
    private TextView classText;
    private TextView dateText;
    private TextView timeText;
    private TextView reminderText;
    private TextView maxParticipantsText;
    private Button toDateBtn;
    private Button toTimeBtn;
    private Button reminderBtn;
    private AppCompatButton doneBtn;
    private DatePickerDialog.OnDateSetListener toDateCallback;
    private Calendar toDateCalendar;
    private String[] NotifyStrings;
    private int notify;
    private int maxParticipants;
    private Course currentCourse;
    private StudyGroup currentStudyGroup;
    private StudyGroup oldStudyGroup;
    private int studyGroupIndex;
    private DismissListener listener;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        toDateCalendar = Calendar.getInstance();
        toDateCalendar.set(Calendar.SECOND, 0);
        toDateCalendar.set(Calendar.MILLISECOND, 0);
        NotifyStrings = User.Student.GetNotifyOpt();
        opt = getArguments().getString("opt");
        if (opt.equals("new"))
        {
            currentStudyGroup = null;
        }
        else
        {
            studyGroupIndex = Integer.parseInt(opt);
            System.out.println(studyGroupIndex);
            currentStudyGroup = User.Student.GetStudyGroupByIndex(studyGroupIndex);
            oldStudyGroup = new StudyGroup(currentStudyGroup);
        }

        this.toDateCallback = new DatePickerDialog.OnDateSetListener()
        {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2)
            {
                toDateCalendar.set(Calendar.YEAR, i);
                toDateCalendar.set(Calendar.MONTH, i1);
                toDateCalendar.set(Calendar.DAY_OF_MONTH, i2);
                SetDateText(dateText, toDateCalendar);
            }
        };

    }

    private void SetDateText(TextView toDateText, Calendar toDateCalendar)
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(PATTERN, Locale.getDefault());
        toDateText.setText(simpleDateFormat.format(toDateCalendar.getTime()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.add_study_group, container, false);
        toolbarTitleText = view.findViewById(R.id.toolbarTitleText);
        pickClassText = view.findViewById(R.id.pickClass);
        toDateBtn = view.findViewById(R.id.toDateButton);
        toTimeBtn = view.findViewById(R.id.toTimeButton);
        reminderBtn = view.findViewById(R.id.reminderButton);
        doneBtn = view.findViewById(R.id.doneButton);
        dateText = view.findViewById(R.id.toDateText);
        timeText = view.findViewById(R.id.toTimeText);
        reminderText = view.findViewById(R.id.reminderText);
        classText = view.findViewById(R.id.classText);
        maxParticipantsText = view.findViewById(R.id.maxParticipants);

        doneBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (ValidateInput())
                {
                    if (currentStudyGroup != null)
                    {
                        currentStudyGroup.setStudyGroupDate(toDateCalendar.getTimeInMillis());
                        currentStudyGroup.setNotify(notify);
                        currentStudyGroup.setMaxParticipants(maxParticipants);
                        User.Student.UpdatedStudyGroup(studyGroupIndex, getContext(), oldStudyGroup);
                    }
                    else
                    {
                        StudyGroup studyGroup = new StudyGroup(currentCourse, toDateCalendar, notify, maxParticipants);
                        User.Student.AddStudyGroup(studyGroup, getContext());
                    }
                    listener.Dismiss();
                }
            }
        });

        toDateBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                new DatePickerDialog(getContext(), R.style.MyDialogTheme, toDateCallback, toDateCalendar.get(Calendar.YEAR), toDateCalendar.get(Calendar.MONTH), toDateCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        toTimeBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener()
                {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1)
                    {
                        SetTimeTextView(timeText, i, i1);
                        toDateCalendar.set(Calendar.HOUR_OF_DAY, i);
                        toDateCalendar.set(Calendar.MINUTE, i1);
                    }
                }, toDateCalendar.get(Calendar.HOUR_OF_DAY), toDateCalendar.get(Calendar.MINUTE), true);
                timePickerDialog.show();
            }
        });

        reminderBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                AddExamDialogListener OKbtn = new AddExamDialogListener()
                {
                    @Override
                    public void OkValue(int value)
                    {
                        notify = value + 1;
                        reminderText.setText(NotifyStrings[notify - 1]);
                    }
                };

                ItemDialog itemDialog = new ItemDialog();
                itemDialog.setStrs(NotifyStrings, OKbtn);
                itemDialog.show(getFragmentManager(), "Picker");
            }
        });


        if (currentStudyGroup == null)
        {
            toolbarTitleText.setText(getResources().getText(R.string.addStudyGroup));
            pickClassText.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    String[] strings = User.Student.GetCourseNames();
                    if (strings.length == 0)
                    {
                        Toast.makeText(getContext(), getResources().getText(R.string.noclassesfound), Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        AddExamDialogListener OKbtn = new AddExamDialogListener()
                        {
                            @Override
                            public void OkValue(int value)
                            {
                                currentCourse = User.Student.GetCourseAtIndex(value);
                                classText.setText(currentCourse.getCourseName());
                            }
                        };

                        ItemDialog itemDialog = new ItemDialog();
                        itemDialog.setStrs(strings, OKbtn);
                        itemDialog.show(getFragmentManager(), "Picker");
                    }
                }
            });
        }

        else
        {
            toolbarTitleText.setText(getResources().getText(R.string.editStudyGroup));
            PutData();
        }
        pickClassText.setPaintFlags(pickClassText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        return view;
    }

    private boolean ValidateInput()
    {
        try
        {
            if (classText.getText().equals("") || dateText.getText().equals("") || timeText.getText().equals("") || reminderText.getText().equals(""))
            {
                throw new Exception(getResources().getString(R.string.missingInfo));
            }
            int index = - 1;

            if (! currentCourse.CheckStudyGroupDate(toDateCalendar, index))
            {
                throw new Exception(getResources().getString(R.string.studygroupontime));
            }
            return true;
        } catch (Exception ex)
        {
            Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void PutData()
    {
        currentCourse = currentStudyGroup.getCourse();
        classText.setText(currentStudyGroup.getCourse().getCourseName());

        toDateCalendar.setTimeInMillis(currentStudyGroup.GetStudyGroupDateAsCalendar().getTimeInMillis());
        SetTimeTextView(timeText, toDateCalendar.get(Calendar.HOUR_OF_DAY), toDateCalendar.get(Calendar.MINUTE));
        SetDateText(dateText, toDateCalendar);

        notify = currentStudyGroup.getNotify();
        reminderText.setText(NotifyStrings[notify - 1]);
    }

    private void SetTimeTextView(TextView toTimeText, int hour, int minute)
    {
        String ampm = "PM";
        String minutestr = String.valueOf(minute);
        if (minute < 10)
        {
            minutestr = "0" + minutestr;
        }
        if (hour < 12)
        {
            ampm = "AM";
        }

        toTimeText.setText(hour + ":" + minutestr + " " + ampm);
    }

    public void setDialogDismissCallback(DismissListener listener)
    {
        this.listener = listener;
    }
}
