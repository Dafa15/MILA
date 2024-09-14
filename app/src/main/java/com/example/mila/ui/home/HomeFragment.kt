import android.os.Build
import java.util.Calendar
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mila.constant.Constant
import com.example.mila.databinding.FragmentHomeBinding
import com.example.mila.ui.chat.ChatActivity
import com.example.mila.ui.home.HomeViewModel
import com.example.mila.util.UserPreference

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var userPreference: UserPreference

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        userPreference = UserPreference(requireContext())
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Set the user's name
        binding.userName.text = userPreference.getString(Constant.KEY_NAME)

        // Set greeting message based on time of day
//        binding.greetings.text = getGreetingMessage()

        // Set up chat button
        binding.buttonChat.setOnClickListener {
            val intent = Intent(requireActivity(), ChatActivity::class.java)
            startActivity(intent)
        }

        return root
    }

//    private fun getGreetingMessage(): String {
//        val calendar = Calendar.getInstance()
//        val hour = calendar.get(Calendar.HOUR_OF_DAY)
//
//        return when (hour) {
//            in 0..11 -> "Selamat pagi"
//            in 12..15 -> "Selamat siang"
//            in 16..18 -> "Selamat sore"
//            else -> "Selamat malam"
//        }
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
