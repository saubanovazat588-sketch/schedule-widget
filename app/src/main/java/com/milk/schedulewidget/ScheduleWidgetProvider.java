package com.milk.schedulewidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.widget.RemoteViews;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ScheduleWidgetProvider extends AppWidgetProvider {
    private static final String ACTION_UPDATE = "com.milk.schedulewidget.UPDATE";
    private static final long INTERVAL = 60000L;

    public static class Lesson {
        public final String start, end, subject, room;
        public Lesson(String start, String end, String subject, String room) {
            this.start = start; this.end = end; this.subject = subject; this.room = room;
        }
    }

    public static List<Lesson> getDay(int dow) {
        List<Lesson> list = new ArrayList<>();
        switch (dow) {
            case 2: list.add(new Lesson("09:20","10:00","Родная (татарская) литература","к4.6.1"));
                    list.add(new Lesson("10:10","10:50","Иностранный (английский) язык","к1.1"));
                    list.add(new Lesson("11:10","11:50","История","к3.3"));
                    list.add(new Lesson("12:10","12:50","Физическая культура","к1.1"));
                    list.add(new Lesson("13:05","13:45","Алгебра","к1.1"));
                    list.add(new Lesson("14:00","14:40","Физика","к4.1"));
                    list.add(new Lesson("14:55","15:35","Биология","к2.15")); break;
            case 3: list.add(new Lesson("08:30","09:10","Физическая культура","к1.1"));
                    list.add(new Lesson("09:20","10:00","Алгебра","к1.1"));
                    list.add(new Lesson("10:10","10:50","Геометрия","к1.1"));
                    list.add(new Lesson("11:10","11:50","Родной (татарский) язык","к4.6.1"));
                    list.add(new Lesson("12:10","12:50","Информатика","к2.13"));
                    list.add(new Lesson("13:05","13:45","География","к3.7"));
                    list.add(new Lesson("14:00","14:40","Химия","к1.1")); break;
            case 4: list.add(new Lesson("09:20","10:00","Литература","к2.6"));
                    list.add(new Lesson("10:10","10:50","Вероятность и статистика","к1.1"));
                    list.add(new Lesson("11:10","11:50","Химия","к1.1"));
                    list.add(new Lesson("12:10","12:50","Иностранный (английский) язык","к1.1"));
                    list.add(new Lesson("13:05","13:45","Русский язык","к2.7"));
                    list.add(new Lesson("14:00","14:40","Физика","к4.1"));
                    list.add(new Lesson("14:55","15:35","Биология","к2.15")); break;
            case 5: list.add(new Lesson("09:20","10:00","Труд (технология)","к1.1"));
                    list.add(new Lesson("10:10","10:50","История","к3.3"));
                    list.add(new Lesson("11:10","11:50","Литература","к2.6"));
                    list.add(new Lesson("12:10","12:50","Алгебра","к1.1"));
                    list.add(new Lesson("13:05","13:45","Геометрия","к1.1"));
                    list.add(new Lesson("14:00","14:40","Музыка","кМузыка")); break;
            case 6: list.add(new Lesson("09:20","10:00","Основы безопасности и защиты Родины","к1.1"));
                    list.add(new Lesson("10:10","10:50","Иностранный (английский) язык","к1.1"));
                    list.add(new Lesson("11:10","11:50","История","к3.3"));
                    list.add(new Lesson("12:10","12:50","География","к3.7"));
                    list.add(new Lesson("13:05","13:45","Русский язык","к2.7"));
                    list.add(new Lesson("14:00","14:40","Русский язык","к2.7")); break;
        }
        return list;
    }

    public static int toMinutes(String hm) {
        String[] p = hm.split(":");
        return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]);
    }

    @Override public void onEnabled(Context c) { super.onEnabled(c); scheduleAlarm(c); }
    @Override public void onDisabled(Context c) { super.onDisabled(c); cancelAlarm(c); }

    @Override public void onUpdate(Context c, AppWidgetManager m, int[] ids) {
        for (int id : ids) updateOne(c, m, id);
    }

    @Override public void onReceive(Context c, Intent i) {
        super.onReceive(c, i);
        String a = i.getAction();
        if (ACTION_UPDATE.equals(a) || Intent.ACTION_SCREEN_ON.equals(a)) {
            AppWidgetManager m = AppWidgetManager.getInstance(c);
            int[] ids = m.getAppWidgetIds(new ComponentName(c, ScheduleWidgetProvider.class));
            for (int id : ids) updateOne(c, m, id);
        }
    }

    private void updateOne(Context c, AppWidgetManager m, int id) {
        RemoteViews rv = new RemoteViews(c.getPackageName(), R.layout.widget_next_lesson);
        Calendar now = Calendar.getInstance();
        int dow = now.get(Calendar.DAY_OF_WEEK);
        int nowMin = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
        String label = "СЛЕДУЮЩИЙ УРОК", time = "—", subject = "Нет уроков", room = "";
        boolean found = false;
        List<Lesson> today = getDay(dow);
        for (Lesson l : today) {
            int s = toMinutes(l.start), e = toMinutes(l.end);
            if (nowMin >= s && nowMin < e) {
                label = "СЕЙЧАС ИДЁТ"; time = l.start + "–" + l.end;
                subject = l.subject; room = l.room; found = true; break;
            }
            if (nowMin < s) {
                time = l.start + "–" + l.end; subject = l.subject; room = l.room; found = true; break;
            }
        }
        if (!found) {
            for (int i = 1; i <= 7; i++) {
                int nd = ((dow - 1 + i) % 7) + 1;
                List<Lesson> nx = getDay(nd);
                if (!nx.isEmpty()) {
                    Lesson f = nx.get(0);
                    time = f.start + "–" + f.end; subject = f.subject; room = f.room; break;
                }
            }
        }
        rv.setTextViewText(R.id.label, label);
        rv.setTextViewText(R.id.time, time);
        rv.setTextViewText(R.id.subject, subject);
        rv.setTextViewText(R.id.room, room.isEmpty() ? "" : "📍 " + room);
        m.updateAppWidget(id, rv);
    }

    private void scheduleAlarm(Context c) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        am.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + INTERVAL, INTERVAL, updateIntent(c));
    }

    private void cancelAlarm(Context c) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        am.cancel(updateIntent(c));
    }

    private PendingIntent updateIntent(Context c) {
        Intent i = new Intent(c, ScheduleWidgetProvider.class);
        i.setAction(ACTION_UPDATE);
        return PendingIntent.getBroadcast(c, 0, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
