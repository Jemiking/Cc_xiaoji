import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  useWindowDimensions,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { colors, spacing, typography, globalStyles } from '../../styles/theme';
import type { NativeStackNavigationProp } from '@react-navigation/native-stack';

type RootStackParamList = {
  Home: undefined;
  Accounting: undefined;
  Memo: undefined;
  Todo: undefined;
};

type HomeScreenProps = {
  navigation: NativeStackNavigationProp<RootStackParamList, 'Home'>;
};

export const HomeScreen: React.FC<HomeScreenProps> = ({ navigation }) => {
  const { width } = useWindowDimensions();

  const menuItems = [
    { title: '记账', route: 'Accounting' },
    { title: '备忘录', route: 'Memo' },
    { title: '待办事项', route: 'Todo' },
  ];

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.content}>
        <Text style={styles.title}>小记</Text>
        <View style={styles.menuContainer}>
          {menuItems.map((item, index) => (
            <TouchableOpacity
              key={index}
              style={styles.menuItem}
              onPress={() => navigation.navigate(item.route as keyof RootStackParamList)}
            >
              <Text style={styles.menuText}>{item.title}</Text>
            </TouchableOpacity>
          ))}
        </View>
      </View>
      <View style={styles.bottomNav}>
        <TouchableOpacity style={styles.navItem}>
          <Text style={styles.navText}>主页</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.navItem}>
          <Text style={styles.navText}>设置</Text>
        </TouchableOpacity>
      </View>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    ...globalStyles.container,
  },
  content: {
    flex: 1,
    padding: spacing.lg,
  },
  title: {
    fontSize: typography.sizes.xlarge,
    fontWeight: 'bold',
    color: colors.text,
    textAlign: 'center',
    marginBottom: spacing.xl,
  },
  menuContainer: {
    gap: spacing.md,
  },
  menuItem: {
    ...globalStyles.button,
    height: 60,
  },
  menuText: {
    ...globalStyles.buttonText,
    fontSize: typography.sizes.medium,
  },
  bottomNav: {
    flexDirection: 'row',
    borderTopWidth: 1,
    borderTopColor: colors.border,
    backgroundColor: colors.primary,
  },
  navItem: {
    flex: 1,
    padding: spacing.md,
    alignItems: 'center',
  },
  navText: {
    color: colors.text,
    fontSize: typography.sizes.small,
  },
}); 