package com.example.firebasecalendarapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private EditText editText;
    private String stringDateSelected;
    private DatabaseReference databaseReference;
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private List<EventModel> eventList;
    private int lastEditedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inisialisasi elemen UI
        calendarView = findViewById(R.id.calendarView);
        editText = findViewById(R.id.editText);
        recyclerView = findViewById(R.id.recyclerView);

        // Set listener untuk perubahan tanggal pada CalendarView
        calendarView.setOnDateChangeListener((calendarView, i, i1, i2) -> {
            stringDateSelected = formatDate(i, i1, i2);
            updateEventList();
        });

        // Inisialisasi tombol "About"
        ImageButton aboutButton = findViewById(R.id.aboutButton);
        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mengarahkan ke AboutActivity saat tombol About diklik
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });

        // Inisialisasi referensi Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("Calendar");

        // Inisialisasi RecyclerView dan Adapter
        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(eventList, new EventAdapter.EventClickListener() {
            @Override
            public void onEditClick(int position) {
                if (position >= 0 && position < eventList.size()) {
                    EventModel event = eventList.get(position);
                    editText.setText(event.getEventName());
                    lastEditedPosition = position;
                } else {
                    // Handle an invalid position, log an error, or show a message.
                }
            }
            @Override
            public void onDeleteClick(int position) {
                // Handle klik tombol delete pada item RecyclerView dengan konfirmasi
                showDeleteConfirmationDialog(position);
            }
        });

        // Konfigurasi RecyclerView
        recyclerView.setAdapter(eventAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void showDeleteConfirmationDialog(int position) {
        // Membuat AlertDialog untuk konfirmasi penghapusan
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Konfirmasi Hapus");
        builder.setMessage("Yakin ingin menghapus data?");

        // Tombol OK (Hapus)
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteEvent(position);
            }
        });

        // Tombol Batal
        builder.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // Tutup dialog tanpa menghapus
            }
        });

        // Menampilkan AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void updateEventList() {
        if (stringDateSelected != null && !stringDateSelected.isEmpty()) {
            databaseReference.child("Event").child(stringDateSelected).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    eventList.clear();

                    if (snapshot.exists()) {
                        for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                            // Check if the eventSnapshot is not null before processing
                            if (eventSnapshot.getValue() != null) {
                                String eventName = eventSnapshot.getValue(String.class);
                                String eventKey = eventSnapshot.getKey();
                                eventList.add(new EventModel(eventKey, stringDateSelected, eventName));
                            }
                        }
                    }

                    // Logging the retrieved event data
                    for (EventModel event : eventList) {
                        if (event != null) {
                            Log.d("EventList", "Event: " + event.getEventName());
                        } else {
                            Log.d("EventList", "Null Event");
                        }
                    }

                    // Tambahkan item null jika tidak ada event
                    if (eventList.isEmpty()) {
                        eventList.add(null);
                    }

                    eventAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle kesalahan saat mengambil data dari Firebase
                    Log.e("FirebaseData", "Error fetching data", error.toException());
                }

            });
        }
    }

    public void buttonSaveEvent(View view) {
        String eventName = editText.getText().toString();
        if (!eventName.isEmpty() && stringDateSelected != null) {
            if (lastEditedPosition != -1) {
                // Jika sedang mengedit, munculkan konfirmasi sebelum mengupdate event
                if (isDateSelectedCorrect()) {
                    updateEventWithConfirmation(lastEditedPosition, eventName);
                    lastEditedPosition = -1;
                } else {
                    // Tampilkan pesan kesalahan jika tanggal yang dipilih tidak sesuai
                    showDateMismatchError();
                }
            } else {
                // Jika tidak sedang mengedit, tambahkan event baru
                String eventKey = databaseReference.child("Event").child(stringDateSelected).push().getKey();
                databaseReference.child("Event").child(stringDateSelected).child(eventKey).setValue(eventName);
                updateEventList();
                clearEditText();
            }
        } else {
            // Tambahkan penanganan jika stringDateSelected null
            Log.e("SaveEvent", "Invalid stringDateSelected");
            Toast.makeText(this, "Pilih tanggal terlebih dahulu", Toast.LENGTH_SHORT).show();
        }
    }

    // Metode untuk menampilkan pesan kesalahan jika tanggal yang dipilih tidak sesuai
    private void showDateMismatchError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Kesalahan");
        builder.setMessage("Maaf, tidak bisa mengupdate event karena tanggal yang dipilih tidak tepat. Mohon periksa kembali.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Metode untuk mengecek apakah tanggal yang dipilih sesuai dengan tanggal yang sedang diedit
    private boolean isDateSelectedCorrect() {
        long selectedDateInMillis = calendarView.getDate();
        String selectedDate = formatDate(selectedDateInMillis);

        Log.d("DateCheck", "stringDateSelected: " + stringDateSelected);
        Log.d("DateCheck", "selectedDate: " + selectedDate);

        return selectedDate.equals(stringDateSelected);
    }


    // Memperbarui nama event di Firebase dengan konfirmasi
    private void updateEventWithConfirmation(int position, String newName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Konfirmasi Update");
        builder.setMessage("Apakah Anda yakin ingin mengupdate event?");

        // Tombol OK (Update)
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateEvent(position, newName);
            }
        });

        // Tombol Batal
        builder.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // Tutup dialog tanpa mengupdate
            }
        });

        // Menampilkan AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Metode untuk memperbarui event di Firebase
    private void updateEvent(int position, String newName) {
        if (position >= 0 && position < eventList.size()) {
            EventModel event = eventList.get(position);
            if (event != null) {
                event.setEventName(newName);
                String eventKey = event.getEventKey();
                databaseReference.child("Event").child(stringDateSelected).child(eventKey).setValue(newName)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // Update berhasil, refresh daftar event
                                    updateEventList();
                                    // Clear EditText setelah berhasil update
                                    clearEditText();
                                } else {
                                    // Gagal melakukan update
                                    Log.e("UpdateEvent", "Failed to update event", task.getException());
                                    Toast.makeText(MainActivity.this, "Gagal mengupdate event", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                eventAdapter.notifyItemChanged(position);
            } else {
                // Log atau tangani kondisi ketika objek event bernilai null
                Log.e("UpdateEvent", "Event object is null at position: " + position);
            }
        } else {
            // Log atau tangani kondisi ketika posisi tidak valid
            Log.e("UpdateEvent", "Invalid position: " + position);
        }
    }

    // Menghapus event dari Firebase
    public void deleteEvent(int position) {
        if (position >= 0 && position < eventList.size()) {
            EventModel event = eventList.get(position);
            String eventKey = event.getEventKey();
            eventList.remove(position);
            eventAdapter.notifyItemRemoved(position);
            databaseReference.child("Event").child(stringDateSelected).child(eventKey).removeValue();
        } else {
            // Log jika posisi yang dihapus tidak valid
            Log.e("DeleteEvent", "Invalid position: " + position);
        }
    }

    // Membersihkan EditText setelah menyimpan event
    private void clearEditText() {
        editText.setText("");
    }

    // Mengubah format tanggal menjadi string dengan format tertentu
    private String formatDate(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH dimulai dari 0
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        return String.format(Locale.getDefault(), "%04d%02d%02d", year, month, day);
    }

    // Metode overloaded untuk formatting int values
    private String formatDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        // Mengonversi tanggal ke waktu dalam milidetik
        long millis = calendar.getTimeInMillis();

        return formatDate(millis);
    }
}
