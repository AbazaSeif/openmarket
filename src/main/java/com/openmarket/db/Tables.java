package com.openmarket.db;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

public class Tables {

	@Table("User")
	public static class User extends Model {}
	
	@Table("Address")
	public static class Address extends Model {}
	
	@Table("Country")
	public static class Country extends Model {}
	
	@Table("Login")
	public static class Login extends Model {}
	
	@Table("Wishlist")
	public static class Wishlist extends Model {}
	
	@Table("wishlist_item")
	public static class WishlistItem extends Model {}
	
	@Table("Seller")
	public static class Seller extends Model {}
	
	@Table("Product")
	public static class Product extends Model {}
	
	@Table("Product_picture")
	public static class ProductPicture extends Model {}
	
	@Table("Product_page")
	public static class ProductPage extends Model {}
	
	@Table("Product_price")
	public static class ProductPrice extends Model {}
	
	
	@Table("Auction")
	public static class Auction extends Model {}

	@Table("Currency")
	public static class Currency extends Model {}
	
	@Table("Time_type")
	public static class TimeType extends Model {}
	
	@Table("Time_span")
	public static class TimeSpan extends Model {}
	
	
}
