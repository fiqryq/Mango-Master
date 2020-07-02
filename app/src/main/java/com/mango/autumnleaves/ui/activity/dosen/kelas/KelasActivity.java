package com.mango.autumnleaves.ui.activity.dosen.kelas;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mango.autumnleaves.R;
import com.mango.autumnleaves.model.Jadwal;
import com.mango.autumnleaves.ui.activity.LoginActivity;
import com.mango.autumnleaves.ui.activity.SplashScreen;
import com.mango.autumnleaves.ui.activity.base.BaseActivity;
import com.mango.autumnleaves.adapter.adapterdosen.KelasAdapter;
import com.mango.autumnleaves.model.Presensi;
import com.mango.autumnleaves.model.SesiKelas;
import com.mango.autumnleaves.model.dosen.UserDosen;
import com.mango.autumnleaves.util.Constant;
import com.shreyaspatil.MaterialDialog.MaterialDialog;
import com.shreyaspatil.MaterialDialog.interfaces.DialogInterface;
import com.shreyaspatil.MaterialDialog.interfaces.OnCancelListener;
import com.shreyaspatil.MaterialDialog.interfaces.OnDismissListener;
import com.shreyaspatil.MaterialDialog.interfaces.OnShowListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.mango.autumnleaves.util.FunctionHelper.Func.getHour;
import static com.mango.autumnleaves.util.FunctionHelper.Func.getNameDay;
import static com.mango.autumnleaves.util.FunctionHelper.Func.getTimeNow;

public class KelasActivity extends BaseActivity implements View.OnClickListener, OnShowListener, OnCancelListener, OnDismissListener {

    private TextView tvMatakuliah, tvDosen, tvKelas, mViewLogMahasiswa , tvSesiPertemuan;
    private EditText mEtMateri, mEtCatatan;
    private Button btnSubmit;
    private MaterialDialog bapdialog;
    private Switch switchSesi;

    public String idDoccument = "";
    public String mataKuliahNow = "";
    public String RuanganNow = "";
    public String Kelas = "";
    public String datKelas = "";
    public String datMatakuliah = "";
    public String datDosen = "";
    public String datRuangan = "";
    public int datPetemuan = 0;
    public String datDocId = "";
    public long jumlahMahasiswa = 0;
    public long radioAlfa;
    public long radioHadir;
    public long radioIzin;
    public long radioSakit;

    private static int UPDATE_TEMP = 6000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kelas);

        tvMatakuliah = findViewById(R.id.tvSesiMatakuliah);
        tvDosen = findViewById(R.id.tvSesiDosen);
        tvKelas = findViewById(R.id.tvSesiRuangan);
        mViewLogMahasiswa = findViewById(R.id.ViewLogMahasiswa);
        mEtMateri = findViewById(R.id.etMateriSesiKelas);
        tvSesiPertemuan = findViewById(R.id.tvSesiPertemuan);
        mEtCatatan = findViewById(R.id.etCatatanSesiKelas);
        btnSubmit = findViewById(R.id.btnSubmit);
        switchSesi = findViewById(R.id.ButtonSwitch);

        Intent intent = getIntent();
        datMatakuliah = intent.getStringExtra("MATAKULIAH");
        datDosen = intent.getStringExtra("DOSEN");
        datKelas = intent.getStringExtra("KELAS");
        datRuangan = intent.getStringExtra("RUANGAN");

        // doccumentsnapshoot untuk mendapatkan dokumen secara spesifik
        DocumentReference docRef = firebaseFirestore.collection("user").document(getFirebaseUserId());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        UserDosen userDosen = new UserDosen();
                        userDosen.setJurusan(document.getString("jurusan"));
                        userDosen.setNip(document.getString("nip"));

                        // Doc Ref Dari user
                        String nipRef = userDosen.getNip();

                        // Querysnapshot untuk mendapatkan semua data dari doccument
                        firebaseFirestore
                                .collection("jadwalDosen")
                                .document(nipRef)
                                .collection("jadwal")
                                .whereEqualTo("hari", getNameDay())
                                .whereLessThan("waktu_mulai", getHour())
                                .orderBy("waktu_mulai", Query.Direction.DESCENDING)
                                .limit(1)
                                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Jadwal jadwal = new Jadwal();
                                        jadwal.setHari(document.getString("hari"));
                                        jadwal.setMatakuliah(document.getString("matakuliah"));
                                        jadwal.setDosen(document.getString("dosen"));
                                        jadwal.setJurusan(document.getString("jurusan"));
                                        jadwal.setKelas(document.getString("kelas"));
                                        jadwal.setPertemuan(document.getLong("pertemuan").intValue());
                                        jadwal.setDocId(document.getString("docId"));
                                        jadwal.setRuangan(document.getString("ruangan"));
                                        jadwal.setWaktu_mulai(document.getString("waktu_mulai"));
                                        jadwal.setWaktu_selesai(document.getString("waktu_selesai"));

                                        datDosen = jadwal.getDosen();
                                        datMatakuliah = jadwal.getMatakuliah();
                                        datRuangan = jadwal.getRuangan();
                                        datKelas = jadwal.getKelas();
                                        datPetemuan = (int) jadwal.getPertemuan();
                                        datDocId = jadwal.getDocId();

                                        tvDosen.setText(": " + datDosen);
                                        tvMatakuliah.setText(": " + datMatakuliah);
                                        tvKelas.setText(": " + datRuangan);
                                        tvSesiPertemuan.setText(": " + String.valueOf(datPetemuan));

                                    }
                                } else {
                                    Log.d("tes", "Error getting documents: ", task.getException());
                                }
                            }
                        });
                    } else {
                        Log.d("gagal", "Documment tidak ada");
                    }
                } else {
                    Log.d("gagal", "gagal", task.getException());
                }
            }
        });

        CheckSesi();
        jadwalRef();
        getdatakelas();

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("data");
        rootRef.child(datKelas).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                jumlahMahasiswa = snapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference hadirCount = FirebaseDatabase.getInstance().getReference().child("data");
        hadirCount.child(datKelas).orderByChild("status").equalTo(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                radioHadir = snapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference IzinCount = FirebaseDatabase.getInstance().getReference().child("data");
        IzinCount.child(datKelas).orderByChild("status").equalTo(2).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                radioIzin = snapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference sakitCount = FirebaseDatabase.getInstance().getReference().child("data");
        sakitCount.child(datKelas).orderByChild("status").equalTo(3).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                radioSakit = snapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference alfaCount = FirebaseDatabase.getInstance().getReference().child("data");
        alfaCount.child(datKelas).orderByChild("status").equalTo(0).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                radioAlfa = snapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mViewLogMahasiswa.setOnClickListener(v -> viewLog());

        switchSesi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                switchSesi.setText("Sesi Aktif");
                updateTrue();
            } else {
                switchSesi.setText("Sesi Tidak Aktif");
                updateFalse();
            }
        });

        bapdialog = new MaterialDialog.Builder(this)
                .setTitle("Submit Bap")
                .setMessage("Apakah Kamu Yakin Akan Submit Bap?")
                .setCancelable(false)
                .setPositiveButton("Submit", (dialogInterface, i) -> {
                    showSuccessToast("Berhasil Submit");
                    PushDataBAP();
                    updatePertemuan();
//                    updateData();
                    switchSesi.setChecked(false);
                    dialogInterface.dismiss();
                })
                .setNegativeButton("Cancel", (dialogInterface, which) -> {
                    Toast.makeText(getApplicationContext(), "Dibatalkan", Toast.LENGTH_SHORT).show();
                    dialogInterface.dismiss();
                })
                .build();

        btnSubmit.setOnClickListener(this::onClick);
    }

    private void getdatakelas() {
        // doccumentsnapshoot untuk mendapatkan dokumen secara spesifik
        DocumentReference docRef = firebaseFirestore.collection("user").document(getFirebaseUserId());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        UserDosen userDosen = new UserDosen();
                        userDosen.setNama(document.getString("nama"));
                        userDosen.setJurusan(document.getString("jurusan"));
                        userDosen.setNip(document.getString("nip"));

                        // Doc Ref Dari user
                        String nipRef = userDosen.getNip();

                        // Querysnapshot untuk mendapatkan semua data dari doccument
                        firebaseFirestore
                                .collection("jadwalDosen")
                                .document(nipRef)
                                .collection("jadwal")
                                .whereEqualTo("hari", getNameDay())
                                .whereLessThan("waktu_mulai", getHour())
                                .orderBy("waktu_mulai", Query.Direction.DESCENDING)
                                .limit(1)
                                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        SesiKelas sesiKelas = new SesiKelas();
                                        sesiKelas.setKelas(document.getString("kelas"));
                                        Kelas = sesiKelas.getKelas();
                                    }
                                } else {
                                    Log.d("tes", "Error getting documents: ", task.getException());
                                }
                            }
                        });
                    } else {
                        Log.d("gagal", "Documment tidak ada");
                    }
                } else {
                    Log.d("gagal", "gagal", task.getException());
                }
            }
        });
    }


    private void viewLog() {
        Intent intent = new Intent(KelasActivity.this, LogMahasiswaActivity.class);
        intent.putExtra("DATAKELAS", Kelas);
        startActivity(intent);
    }

    private void CheckSesi() {
        firebaseFirestore
                .collection("prodi")
                .document("rpla")
                .collection("kelas")
                .document(datKelas)
                .collection("jadwal")
                .whereEqualTo("hari", getNameDay())
                .whereLessThan("waktu_mulai", getHour())
                .orderBy("waktu_mulai", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            idDoccument = documentSnapshot.getId();
                            if (documentSnapshot.getString("sesi").equals(Constant.SESI_AKTIF)) {
                                switchSesi.setChecked(true);
                            } else {
                                switchSesi.setChecked(false);
                            }
                        }
                    }
                });
    }

    private void jadwalRef() {
        // doccumentsnapshoot untuk mendapatkan dokumen secara spesifik
        DocumentReference docRef = firebaseFirestore.collection("user").document(getFirebaseUserId());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        UserDosen userDosen = new UserDosen();
                        userDosen.setNama(document.getString("nama"));
                        userDosen.setJurusan(document.getString("jurusan"));
                        userDosen.setNip(document.getString("nip"));

                        // Doc Ref Dari user
                        String nipRef = userDosen.getNip();

                        // Querysnapshot untuk mendapatkan semua data dari doccument
                        firebaseFirestore
                                .collection("jadwalDosen")
                                .document(nipRef)
                                .collection("jadwal")
                                .whereEqualTo("hari", getNameDay())
                                .whereLessThan("waktu_mulai", getHour())
                                .orderBy("waktu_mulai", Query.Direction.DESCENDING)
                                .limit(1)
                                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        SesiKelas sesiKelas = new SesiKelas();
                                        sesiKelas.setHari(document.getString("hari"));
                                        sesiKelas.setMatakuliah(document.getString("matakuliah"));
                                        sesiKelas.setDosen(document.getString("dosen"));
                                        sesiKelas.setRuangan(document.getString("ruangan"));
                                        sesiKelas.setWaktu_mulai(document.getString("waktu_mulai"));
                                        sesiKelas.setWaktu_selesai(document.getString("waktu_selesai"));

                                        int selesai = Integer.parseInt(document.getString("waktu_selesai").replace(":", ""));
                                        int sekarang = Integer.parseInt(getHour().replace(":", ""));

                                        if (sekarang <= selesai) {
                                            mataKuliahNow = sesiKelas.getMatakuliah();
                                            RuanganNow = sesiKelas.getRuangan();
                                        }
                                    }
                                } else {
                                    Log.d("tes", "Error getting documents: ", task.getException());
                                }
                            }
                        });
                    } else {
                        Log.d("gagal", "Documment tidak ada");
                    }
                } else {
                    Log.d("gagal", "gagal", task.getException());
                }
            }
        });
    }

    private void PushDataBAP() {
        Map<String, Object> dataBap = new HashMap<>();
        dataBap.put("matakuliah", mataKuliahNow);
        dataBap.put("jam", getHour());
        dataBap.put("waktu", getTimeNow());
        dataBap.put("ruangan", RuanganNow);
        dataBap.put("materi", mEtMateri.getText().toString());
        dataBap.put("catatan", mEtCatatan.getText().toString());
        dataBap.put("pertemuan", datPetemuan);
        dataBap.put("hadir",radioHadir);
        dataBap.put("sakit",radioSakit);
        dataBap.put("izin",radioIzin);
        dataBap.put("alfa",radioAlfa);
        dataBap.put("jumlahMhs", jumlahMahasiswa);
        dataBap.put("created", new Timestamp(new Date()));
        dataBap.put("kelas", Kelas);

        firebaseFirestore
                .collection("bap")
                .document(getFirebaseUserId())
                .collection("data")
                .add(dataBap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {

            }
        });
    }

    private void updateTrue() {
        DocumentReference documentReference = firebaseFirestore
                .collection("prodi")
                .document("rpla")
                .collection("kelas")
                .document(datKelas)
                .collection("jadwal")
                .document(idDoccument);

        Map<String, Object> map = new HashMap<>();
        map.put("sesi", Constant.SESI_AKTIF);
        documentReference.update(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Handle Success Update
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showErrorToast("Gagal Buka Sesi");
            }
        });
    }

    private void updateFalse() {
        DocumentReference documentReference = firebaseFirestore
                .collection("prodi")
                .document("rpla")
                .collection("kelas")
                .document(datKelas)
                .collection("jadwal")
                .document(idDoccument);

        Map<String, Object> map = new HashMap<>();
        map.put("sesi", Constant.SESI_TIDAK_AKTIF);
        documentReference.update(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Handle Success Update
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showErrorToast("Gagal Tutup Sesi");
            }
        });
    }

//    private void updateData() {
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Map<String, Object> updateKehadiran = new HashMap<>();
//                updateKehadiran.put("status",0);
//                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("data").child(datKelas).addValueEventListener();
//                finish();
//            }
//        }, UPDATE_TEMP);
//    }

    private void updatePertemuan(){
        Map<String, Object> updatePertemuan = new HashMap<>();
        updatePertemuan.put("pertemuan", datPetemuan + 1);
        firebaseFirestore
                .collection("jadwalDosen")
                .document(getNipDosen())
                .collection("jadwal")
                .document(datDocId)
                .update(updatePertemuan);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSubmit:
                if (TextUtils.isEmpty(mEtMateri.getText().toString())) {
                    Toast.makeText(mActivity, "Harap Isi Form Materi", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(mEtMateri.getText().toString()) && TextUtils.isEmpty(mEtCatatan.getText().toString())) {
                    Toast.makeText(mActivity, "Harap Isi Form", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(mEtCatatan.getText().toString()) && TextUtils.isEmpty(mEtMateri.getText().toString())) {
                    Toast.makeText(mActivity, "Harap Isi Form", Toast.LENGTH_SHORT).show();}
                else {
                    bapdialog.show();
                }
        }
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {

    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {

    }

    @Override

    public void onShow(DialogInterface dialogInterface) {

    }

}
