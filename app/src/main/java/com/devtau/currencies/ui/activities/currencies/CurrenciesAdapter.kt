package com.devtau.currencies.ui.activities.currencies

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.devtau.currencies.R
import com.devtau.currencies.data.model.Currency
import com.devtau.currencies.util.Logger
import com.devtau.currencies.util.round
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.list_item_currency.*

class CurrenciesAdapter(
    private val listener: CurrenciesContract.ListListener
): RecyclerView.Adapter<CurrenciesAdapter.CurrenciesViewHolder>() {

    private var currencies = ArrayList<Currency>()


    //<editor-fold desc="Framework overrides">
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrenciesViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.list_item_currency, parent, false)
        return CurrenciesViewHolder(
            view
        )
    }

    override fun onBindViewHolder(holder: CurrenciesViewHolder, position: Int) {
        if (position == 0) holder.setIsRecyclable(false)
        val currency = currencies[position]

        currency.flag?.let { Glide.with(holder.image)
            .load(it)
            .transform(CircleCrop())
            .into(holder.image)
        }
        holder.currencyCode.text = currency.code
        holder.currencyName.text = currency.name
        holder.currencyCourse.setText(currency.converted.round())

        holder.containerView.setOnClickListener {
            listener.rowSelected(holder.adapterPosition)
        }
        holder.currencyCourse.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) listener.editTextSelected(holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int = currencies.size
    //</editor-fold>


    //<editor-fold desc="Public methods">
    fun setList(newList: List<Currency>) {
        Logger.d(LOG_TAG, "setList. newList size=${newList.size}")
        val listWasEmpty = currencies.isEmpty()
        currencies.clear()
        currencies.addAll(newList)

        if (listWasEmpty) notifyDataSetChanged()
        else notifyItemRangeChanged(1, itemCount, currencies)
    }

    fun provideCurrencies() = currencies
    //</editor-fold>


    //<editor-fold desc="Private methods">
    //</editor-fold>


    class CurrenciesViewHolder(override val containerView: View):
        RecyclerView.ViewHolder(containerView), LayoutContainer


    companion object {
        private const val LOG_TAG = "CurrenciesAdapter"
    }
}