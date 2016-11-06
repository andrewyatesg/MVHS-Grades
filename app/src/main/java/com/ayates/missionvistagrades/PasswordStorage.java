package com.ayates.missionvistagrades;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * Created by ayates on 11/5/16.
 */
public class PasswordStorage
{
    private static final String cryptoKey = "H3ll0_There_M8tY";
    private static final String PREFS_NAME = "preferences";
    private static final String PREF_UNAME = "Username";
    private static final String PREF_PASSWORD = "Password";

    private String username;
    private String password;

    public PasswordStorage(String username, String password)
    {
        this.username = username;
        this.password = password;
    }

    public void saveCredentials(Context context)
    {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString(PREF_UNAME, encrypt(username));
        editor.putString(PREF_PASSWORD, encrypt(password));
        editor.commit();

        Log.d(LoginPanel.TAG, "Credentials have been saved.");
    }

    public void loadCredentials(Context context)
    {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);

        // Get value
        username = decrypt(settings.getString(PREF_UNAME, ""));
        password = decrypt(settings.getString(PREF_PASSWORD, ""));

        Log.d(LoginPanel.TAG, "Credentials have been loaded.");
    }

    private String encrypt(String value)
    {
        try
        {
            DESKeySpec keySpec = new DESKeySpec(cryptoKey.getBytes("UTF8"));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey key = keyFactory.generateSecret(keySpec);

            byte[] clearText = value.getBytes("UTF8");
            // Cipher is not thread safe
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            String encrypedValue = Base64.encodeToString(cipher.doFinal(clearText), Base64.DEFAULT);
            return encrypedValue;

        } catch (InvalidKeyException e)
        {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        } catch (InvalidKeySpecException e)
        {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        } catch (BadPaddingException e)
        {
            e.printStackTrace();
        } catch (NoSuchPaddingException e)
        {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e)
        {
            e.printStackTrace();
        }
        return value;
    }

    private String decrypt(String value)
    {
        try
        {
            DESKeySpec keySpec = new DESKeySpec(cryptoKey.getBytes("UTF8"));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey key = keyFactory.generateSecret(keySpec);

            byte[] encrypedPwdBytes = Base64.decode(value, Base64.DEFAULT);
            // cipher is not thread safe
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decrypedValueBytes = (cipher.doFinal(encrypedPwdBytes));

            String decrypedValue = new String(decrypedValueBytes);
            return decrypedValue;

        }
        catch (InvalidKeyException e)
        {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        catch (InvalidKeySpecException e)
        {
            e.printStackTrace();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        catch (BadPaddingException e)
        {
            e.printStackTrace();
        }
        catch (NoSuchPaddingException e)
        {
            e.printStackTrace();
        }
        catch (IllegalBlockSizeException e)
        {
            e.printStackTrace();
        }
        return value;
    }

    public String getUsername()
    {
        return username;
    }

    public PasswordStorage setUsername(String username)
    {
        this.username = username;
        return this;
    }

    public String getPassword()
    {
        return password;
    }

    public PasswordStorage setPassword(String password)
    {
        this.password = password;
        return this;
    }
}
