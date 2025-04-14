package com.example.multitradex.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.multitradex.models.Business;
import com.example.multitradex.models.CartItem;
import com.example.multitradex.models.FavoriteProduct;
import com.example.multitradex.models.Order;
import com.example.multitradex.models.OrderItem;
import com.example.multitradex.models.Payment;
import com.example.multitradex.models.Product;
import com.example.multitradex.models.User;
import com.example.multitradex.utils.Converters;

@Database(
        entities = {
                User.class,
                Business.class,
                Product.class,
                CartItem.class,
                Order.class,
                OrderItem.class,
                Payment.class,
                FavoriteProduct.class   // ✅ NEW ENTITY
        },
        version = 7, // ⬅️ bumped from 6 to 7
        exportSchema = false
)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    // DAO Abstract Methods
    public abstract UserDao userDao();
    public abstract BusinessDao businessDao();
    public abstract ProductDao productDao();
    public abstract CartDao cartDao();
    public abstract OrderDao orderDao();
    public abstract OrderItemDao orderItemDao();
    public abstract PaymentDao paymentDao();
    public abstract FavoriteProductDao favoriteProductDao(); // ✅ NEW DAO

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "multi_trade_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
