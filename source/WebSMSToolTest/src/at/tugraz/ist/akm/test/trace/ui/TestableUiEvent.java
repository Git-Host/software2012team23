package at.tugraz.ist.akm.test.trace.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.trace.ui.UiEvent;

public class TestableUiEvent extends UiEvent
{
    private Date mTestDate = null;
    public int mTestDrawable = R.drawable.ic_action_hardware_computer;
    public String mTestDescription = null;
    public String mTestDetail = null;
    public String mTestTitle = null;


    public TestableUiEvent(Date date)
    {
        mTestDate = new Date(date.getTime());
    }


    @Override
    public String getDate()
    {
        DateFormat printFormat = SimpleDateFormat.getDateInstance();
        return printFormat.format(mTestDate);
    }


    @Override
    public String getDescription()
    {
        return mTestDescription;
    }


    @Override
    public String getDetail()
    {
        return mTestDetail;
    }


    @Override
    public int getDrawableIconId()
    {
        return mTestDrawable;
    }


    @Override
    public String getTime()
    {
        DateFormat printFormat = SimpleDateFormat.getTimeInstance();
        return printFormat.format(mTestDate);
    }


    @Override
    public String getTitle()
    {
        return mTestTitle;
    }

}
